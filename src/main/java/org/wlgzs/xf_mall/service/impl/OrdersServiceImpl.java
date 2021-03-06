package org.wlgzs.xf_mall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.wlgzs.xf_mall.dao.OrdersRepository;
import org.wlgzs.xf_mall.dao.ProductRepository;
import org.wlgzs.xf_mall.dao.UserIntegralRepository;
import org.wlgzs.xf_mall.dao.UserRepository;
import org.wlgzs.xf_mall.entity.Orders;
import org.wlgzs.xf_mall.entity.Product;
import org.wlgzs.xf_mall.entity.User;
import org.wlgzs.xf_mall.entity.UserIntegral;
import org.wlgzs.xf_mall.filter.DemoFilter;
import org.wlgzs.xf_mall.service.OrdersService;
import org.wlgzs.xf_mall.util.AlipayConfig;
import org.wlgzs.xf_mall.util.IdsUtil;
import org.wlgzs.xf_mall.util.PageUtil;
import org.wlgzs.xf_mall.util.RandonNumberUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @Auther: 阿杰
 * @Date: 2018/4/20 20:52
 * @Description: 订单
 */
@Service
public class OrdersServiceImpl implements OrdersService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DemoFilter.class);

    @Resource
    private OrdersRepository ordersRepository;
    @Resource
    private ProductRepository productRepository;
    @Resource
    private UserRepository userRepository;
    @Resource
    private UserIntegralRepository userIntegralRepository;

    //后台遍历订单
    @Override
    public Page<Orders> getOrdersList(String order_number, int page, int limit) {
        Sort sort = new Sort(Sort.Direction.DESC, "orderId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        Specification<Orders> specification = new PageUtil<Orders>(order_number).getPage("order_number");
        return ordersRepository.findAll(specification, pageable);
    }

    //后台按照id查找订单
    @Override
    public Orders findOrdersById(long orderId) {
        return ordersRepository.findById(orderId);
    }
    @Override
    public List<Orders> findOrdersByNumber(long orderId) {
        Orders orders = ordersRepository.findById(orderId);
        return ordersRepository.findByNumber(orders.getOrder_number());
    }

    //按照订单号查询订单
    public List<Orders> findOrdersByOrderNumber(String order_number) {
        return ordersRepository.findByOrderNumber(order_number);
    }

    //后台修改订单
    @Override
    public String edit(long orderId, HttpServletRequest request) {
        Orders orders = ordersRepository.findById(orderId);
        if (orders != null) {
            orders.setAddress_name(request.getParameter("address_name"));
            orders.setAddress_phone(request.getParameter("address_phone"));
            orders.setAddress_shipping(request.getParameter("address_shipping"));
            ordersRepository.save(orders);
            return "成功";
        }
        return "未知错误";
    }

    //后台删除订单
    @Override
    public String delete(long orderId) {
        Orders orders = ordersRepository.findById(orderId);
        if (orders != null) {
            ordersRepository.deleteById(orderId);
            return "成功";
        }
        return "未知错误";
    }

    //按照用户名查询订单
    @Override
    public List<Orders> findOrdersByUserName(String user_name) {
        return ordersRepository.findByUserName(user_name);
    }

    //支付   添加订单
    @Override
    public void save(HttpServletRequest request, HttpServletResponse response, String productId, String shoppingCount) {

        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String orderNumber = (String) session.getAttribute("orderNumber");
        productId = (String) session.getAttribute("productId");
        shoppingCount = (String) session.getAttribute("shoppingCount");
        long userId = user.getUserId();

        productId = productId.substring(1, productId.length() - 1);
        logger.info(productId);
        long[] Ids = IdsUtil.IdsUtils(productId);
        shoppingCount = shoppingCount.substring(1, shoppingCount.length() - 1);
        long[] shoppingCounts = IdsUtil.IdsUtils(shoppingCount);

        String address_name = (String) session.getAttribute("address_name"); //收货人
        String address_phone = (String) session.getAttribute("address_phone"); //收货人电话
        String address_shipping = (String) session.getAttribute("address_shipping"); //收货地址
        String WIDtotal_amount = (String) session.getAttribute("WIDtotal_amount"); //收货地址
        for (int i = 0; i < Ids.length; i++) {
            Product product = productRepository.findById(Ids[i]);
            //商品表
            product.setProduct_inventory((int) (product.getProduct_inventory() - shoppingCounts[i]));
            productRepository.save(product);
            //订单表
            Orders order = new Orders();
            order.setAddress_name(address_name); //收货人
            order.setAddress_phone(address_phone); //收货人电话
            order.setAddress_shipping(address_shipping); //收货地址
            order.setOrder_expressNumber(RandonNumberUtils.getOrderIdByUUId());  //快递单号
            order.setOrder_freight(0);  //运费
            order.setOrder_number(orderNumber);  //订单编号
            Date data = new Date();
            order.setOrder_purchaseTime(data); //下单时间
            //int count = Integer.parseInt(request.getParameter("shoppingCart_count"));
            order.setOrder_quantity((int) shoppingCounts[i]);  //购买数量
            logger.info(String.valueOf(shoppingCounts[i]));
            float amount = Float.parseFloat(WIDtotal_amount);
            order.setProduct_PaidPrice(amount); //实付金额
            order.setProductId(Ids[i]); //商品id
            order.setProduct_isRedeemable(product.getProduct_isRedeemable());  //该商品是否积分兑换
            order.setProduct_keywords(product.getProduct_keywords()); //商品关键字
            order.setProduct_mallPrice(product.getProduct_mallPrice());//商城价
            String img = null;
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
            }
            order.setProduct_picture(img); //商城图片
            order.setProduct_specification(product.getProduct_specification());  //商品规格信息
            order.setUserId(userId);//用户id
            order.setProduct_needPoints(product.getProduct_needPoints()); //购买此商品需要多少积分
            order.setUser_name(user.getUser_name()); //用户名
            ordersRepository.save(order);
            //用户积分
            user.setUserIntegral(user.getUserIntegral() + product.getProduct_getPoints());
            userRepository.save(user);
            //积分表
            UserIntegral userIntegral = new UserIntegral();
            userIntegral.setOrder_purchaseTime(data);
            userIntegral.setUserId(userId);
            userIntegral.setProduct_keyword(product.getProduct_keywords());
            userIntegral.setProduct_picture(img);
            userIntegral.setUserIntegral_vary(product.getProduct_getPoints());
            userIntegralRepository.save(userIntegral);
        }
    }

    //用户全部订单
    @Override
    public List<Orders> userOrderList(long userId) {
        return ordersRepository.userOrderList(userId);
    }

    //待收货订单
    @Override
    public List<Orders> userUnacceptedOrder(long userId) {
        return ordersRepository.userUnacceptedOrder(userId);
    }

    //待评价订单
    @Override
    public List<Orders> userUnEstimateOrder(long userId) {
        return ordersRepository.unEstimateOrder(userId);
    }

    //收货
    @Override
    public void userAccepted(long orderId) {
        Orders orders = ordersRepository.findById(orderId);
        List<Orders> ordersList = ordersRepository.findByNumber(orders.getOrder_number());
        for (Orders anOrdersList : ordersList) {
            anOrdersList.setOrder_status("待评价");
        }
        ordersRepository.save(orders);
    }

    //积分兑换商品
    @Override
    public void estimatePaySave(HttpServletRequest request, long productId) {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        long userId = user.getUserId();
        //User user = userRepository.findById(userId);
        Product product = productRepository.findById(productId);
        //商品表
        product.setProduct_inventory(product.getProduct_inventory() - 1);
        productRepository.save(product);
        //订单表
        Orders order = new Orders();
        order.setAddress_name(request.getParameter("address_name")); //收货人
        order.setAddress_phone(request.getParameter("address_phone")); //收货人电话
        order.setAddress_shipping(request.getParameter("address_shipping")); //收货地址
        order.setOrder_expressNumber(RandonNumberUtils.getOrderIdByUUId());  //快递单号
        order.setOrder_freight(0);  //运费
        order.setOrder_number(RandonNumberUtils.getOrderIdByUUId());  //订单编号
        Date data = new Date();
        order.setOrder_purchaseTime(data); //下单时间
        order.setOrder_quantity(1);  //购买数量
        order.setProduct_PaidPrice(0); //实付金额
        order.setProductId(productId); //商品id
        order.setProduct_isRedeemable(1);  //该商品是否积分兑换
        order.setProduct_keywords(product.getProduct_keywords()); //商品关键字
        order.setProduct_mallPrice(product.getProduct_mallPrice());//商城价
        String img = product.getProduct_picture();
        if (product.getProduct_picture().contains(",")) {
            img = product.getProduct_picture();
            img = img.substring(0, img.indexOf(","));
        }
        order.setProduct_picture(img); //商城图片
        order.setProduct_specification(product.getProduct_specification());  //商品规格信息
        order.setUserId(userId);//用户id
        order.setProduct_needPoints(product.getProduct_needPoints()); //购买此商品需要多少积分
        order.setUser_name(user.getUser_name()); //用户名
        order.setOrder_methodOfPurchase("积分兑换");
        order.setOrder_status("交易成功");

        ordersRepository.save(order);
        user.setUserIntegral(user.getUserIntegral() - product.getProduct_needPoints());
        userRepository.save(user);
        //积分表
        UserIntegral userIntegral = new UserIntegral();
        userIntegral.setOrder_purchaseTime(data);
        userIntegral.setUserId(userId);
        userIntegral.setProduct_keyword(product.getProduct_keywords());
        userIntegral.setProduct_picture(img);
        userIntegral.setUserIntegral_vary(-product.getProduct_needPoints());
        userIntegralRepository.save(userIntegral);
    }

    /*@Override
    public void aliReturn(HttpServletResponse response, HttpServletRequest request) throws IOException, AlipayApiException {
        PrintWriter out = response.getWriter();
        //获取支付宝GET过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名
        //——请在这里编写您的程序（以下代码仅作参考）——
        if(signVerified) {
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //付款金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");
            out.println("trade_no:"+trade_no+"<br/>out_trade_no:"+out_trade_no+"<br/>total_amount:"+total_amount);
        }else {
            out.println("验签失败");
        }
    }*/

    //后台订单查询
    @Override
    public Page<Orders> adminSearchOrder(String order_word, int page, int limit) {
        Sort sort = new Sort(Sort.Direction.DESC, "orderId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        Specification<Orders> specification = new PageUtil<Orders>(order_word).getPage("address_name", "order_number", "user_name", "product_keywords");
        return ordersRepository.findAll(specification, pageable);
    }

    //前台查询
    @Override
    public Page<Orders> searchOrder(String order_word,int page, int limit,long userId) {
        String id=String.valueOf(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "orderId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        Specification<Orders> specification = new PageUtil<Orders>(order_word).getPages(id,"product_specification", "order_number", "user_name", "product_keywords");
        return ordersRepository.findAll(specification, pageable);
    }

    @Override
    public long searchProductCount(long productId) {
        return ordersRepository.count(productId);
    }

    @Override
    public void deleteOrders(String orderId) {
        long[] ids = IdsUtil.IdsUtils(orderId);
        ordersRepository.deleteByIds(ids);
    }

    //退款
    @Override
    public void refund(long orderId, HttpServletResponse response,HttpSession session) throws IOException, AlipayApiException {
        User user = (User) session.getAttribute("user");
        long userId = user.getUserId();
        Orders orders = ordersRepository.findById(orderId);
        List<Orders> ordersList = ordersRepository.findByNumber(orders.getOrder_number());
        long[] Ids = new long[ordersList.size()];
        long[] shoppingCounts = new long[ordersList.size()];
        for (int i = 0; i < ordersList.size(); i++) {
            Ids[i] = ordersList.get(i).getProductId();
            shoppingCounts[i] = ordersList.get(i).getOrder_quantity();
            logger.info("修改订单状态");
            ordersList.get(i).setOrder_status("已退款");
        }
        //ordersRepository.saveAll(ordersList);
        for (int i = 0; i < Ids.length; i++) {
            Product product = productRepository.findById(Ids[i]);
            //商品表
            product.setProduct_inventory((int) (product.getProduct_inventory() + shoppingCounts[i]));
            logger.info("返回商品库存");
            productRepository.save(product);
            //用户积分
            user.setUserIntegral (user.getUserIntegral() + product.getProduct_getPoints());
            logger.info("返回用户积分");
            userRepository.save(user);
            //积分表
            UserIntegral userIntegral = userIntegralRepository.findByUserIdAndProductId(userId,ordersList.get(0).getOrder_purchaseTime(),ordersList.get(0).getProduct_keywords());
            if(userIntegral!=null){
                logger.info("删除积分记录");
                userIntegralRepository.deleteById(userIntegral.getUserIntegralId());
            }
        }
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);
        //设置请求参数
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
        //商户订单号，商户网站订单系统中唯一订单号
        logger.info(orders.getOrder_number());
        String out_trade_no = orders.getOrder_number();
        //需要退款的金额，该金额不能大于订单金额，必填
        String refund_amount = String.valueOf(ordersList.get(0).getProduct_PaidPrice());
        //标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
        String out_request_no = UUID.randomUUID().toString();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"refund_amount\":\""+ refund_amount +"\","
                + "\"out_request_no\":\""+ out_request_no +"\"}");
        //请求
        String result = alipayClient.execute(alipayRequest).getBody();
        logger.info(result);
        //输出
        out.println(result);
    }

    //用户的订单
    @Override
    public Map<String, List> userOrder(long userId) {
        List<Orders> orders = ordersRepository.userOrderList(userId);
        //List<String> orderNumbers = ordersRepository.findOrderNumbers(userId);
        List<Orders> ordersT = ordersRepository.findOrders(userId);
        List<String> ordersTw = new ArrayList<>();
        for (Orders anOrdersT : ordersT) {
            ordersTw.add(anOrdersT.getOrder_number());
        }
        //去重 利用set顺序不变
        Set<String> set = new HashSet<>();
        List<String> orderNumbers = new ArrayList<>();
        for (String cd : ordersTw) {
            if (set.add(cd)) {
                orderNumbers.add(cd);
            }
        }
        logger.info(orders.size()+"orders的长度");
        logger.info(orderNumbers.size()+"orderNumber的长度");

        Map<String, List> map = new HashMap<>();
        int m = 0;
        List<Orders> ordersTwo = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            if (!ordersTwo.contains(orders.get(i))) {
                int n = 0;
                List<Orders> ordersList = new ArrayList<>();
                for (int j = i + 1; j < orders.size(); j++) {
                    if (orders.get(i).getOrder_number().equals(orders.get(j).getOrder_number())) {
                        if (n == 0) {
                            ordersList.add(orders.get(i));
                            ordersTwo.add(orders.get(i));
                        }
                        ordersList.add(orders.get(j));
                        ordersTwo.add(orders.get(j));
                        n++;
                    }
                }
                if(n==0){
                    ordersList.add(orders.get(i));
                }
                logger.info("第" + i + "次循环，订单集合：" + ordersList);
                if (ordersList.size() != 0) {
                    //logger.info("m的值:" + m);
                    map.put(orderNumbers.get(m), ordersList);
                    m++;
                }
            }
        }
        return map;
    }

    @Override
    public List<String> findOrderNumbers(long userId) {
        List<Orders> ordersT = ordersRepository.findOrders(userId);
        List<String> ordersTw = new ArrayList<>();
        for (Orders anOrdersT : ordersT) {
            ordersTw.add(anOrdersT.getOrder_number());
        }
        //去重 利用set顺序不变
        Set<String> set = new HashSet<>();
        List<String> orderNumbers = new ArrayList<>();
        for (String cd : ordersTw) {
            if (set.add(cd)) {
                orderNumbers.add(cd);
            }
        }
        return orderNumbers;
        //return ordersRepository.findOrderNumbers(userId);
    }

    @Override
    public void ali(HttpServletResponse response, HttpServletRequest request) throws AlipayApiException, IOException {
        response.setContentType("text/html;charset=utf-8");

        PrintWriter out = response.getWriter();
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);
        //设置请求参数
        AlipayTradePagePayRequest aliPayRequest = new AlipayTradePagePayRequest();
        aliPayRequest.setReturnUrl(AlipayConfig.return_url);
        aliPayRequest.setNotifyUrl(AlipayConfig.notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String orderNumber = RandonNumberUtils.getOrderIdByUUId();
        HttpSession session = request.getSession(true);
        session.setAttribute("orderNumber",orderNumber);
        session.setAttribute("productId",request.getParameter("productId"));
        session.setAttribute("shoppingCount",request.getParameter("shoppingCount"));
        session.setAttribute("WIDtotal_amount",request.getParameter("WIDtotal_amount"));
        session.setAttribute("address_name",request.getParameter("address_name"));
        session.setAttribute("address_phone",request.getParameter("address_phone"));
        session.setAttribute("address_shipping",request.getParameter("address_shipping"));
        logger.info(orderNumber +"------------------");
        //付款金额，必填
        String total_amount = request.getParameter("WIDtotal_amount");
        logger.info(total_amount+"-------------------");
        //订单名称，必填
        String subject = "支付宝沙箱支付";
        aliPayRequest.setBizContent("{\"out_trade_no\":\"" + orderNumber + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        //请求
        String result = alipayClient.pageExecute(aliPayRequest).getBody();
        //输出
        out.println(result);
    }
}