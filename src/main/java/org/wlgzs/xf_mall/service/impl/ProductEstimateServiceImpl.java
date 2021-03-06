package org.wlgzs.xf_mall.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wlgzs.xf_mall.dao.OrdersRepository;
import org.wlgzs.xf_mall.dao.ProductEstimateRepository;
import org.wlgzs.xf_mall.entity.Orders;
import org.wlgzs.xf_mall.entity.ProductEstimate;
import org.wlgzs.xf_mall.service.ProductEstimateService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @Auther: 李晓珊
 * @Date: 2018/4/23 13:16
 * @Description: 商品评价
 */
@Service
public class ProductEstimateServiceImpl implements ProductEstimateService {
    @Resource
    private ProductEstimateRepository productEstimateRepository;
    @Resource
    private OrdersRepository ordersRepository;

    //添加评论
    @Override
    public void save(HttpServletRequest request, MultipartFile[] myFileNames, HttpSession session, long orderId){
        String realName = "";
        String[] str = new String[myFileNames.length];
        for (int i = 0; i < myFileNames.length; i++) {
            if (!Objects.equals(myFileNames[i].getOriginalFilename(), "")) {
                String fileName = myFileNames[i].getOriginalFilename();
                assert fileName != null;
                String fileNameExtension = fileName.substring(fileName.indexOf("."));
                // 生成实际存储的真实文件名
                realName = UUID.randomUUID().toString() + fileNameExtension;
                // "/upload"是你自己定义的上传目录
                String realPath = session.getServletContext().getRealPath("/upload");
                File uploadFile = new File(realPath, realName);
                try {
                    myFileNames[i].transferTo(uploadFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!Objects.equals(myFileNames[i].getOriginalFilename(), "")) {
                str[i] = request.getContextPath() + "/upload/" + realName;
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < str.length; i++) {
            if (!Objects.equals(myFileNames[i].getOriginalFilename(), "")) {
                stringBuffer.append(str[i]).append(",");
            }
        }
        String estimate_img = stringBuffer.substring(0, stringBuffer.length() - 1);

        ProductEstimate productEstimate = new ProductEstimate();
        productEstimate.setEstimate_content(request.getParameter("estimate_content"));
        int estimate_grade = Integer.parseInt(request.getParameter("estimate_grade"));
        productEstimate.setEstimate_grade(estimate_grade);
        productEstimate.setEstimate_img(estimate_img);
        int estimate_is_nameless = 0;
        if(request.getParameter("estimate_is_nameless")!=null){
            estimate_is_nameless = Integer.parseInt(request.getParameter("estimate_is_nameless"));
        }
        productEstimate.setEstimate_isNameless(estimate_is_nameless);
        Date date = new Date();
        productEstimate.setEstimate_time(date);
        Orders orders = ordersRepository.findById(orderId);
        productEstimate.setUserId(orders.getUserId());
        productEstimate.setUser_name(orders.getUser_name());
        productEstimate.setOrderId(orderId);
        productEstimate.setProductId(orders.getProductId());
        productEstimate.setProduct_keywords(orders.getProduct_keywords());
        productEstimate.setProduct_mallPrice(orders.getProduct_mallPrice());
        productEstimate.setProduct_picture(orders.getProduct_picture());
        productEstimate.setProduct_specification(orders.getProduct_specification());
        productEstimateRepository.save(productEstimate);
        orders.setOrder_status("交易成功");
        ordersRepository.save(orders);
    }

    //个人评价展示
    @Override
    public List<ProductEstimate> findEstimateById(long userId) {
        return productEstimateRepository.findByUserId(userId);
    }

    //商品评价展示
    @Override
    public List<ProductEstimate> findEstimateByproductId(long productId) {
        return productEstimateRepository.findByProductId(productId);
    }

    //修改为匿名评价
    @Override
    public void changeEstimate(long estimateId) {
        ProductEstimate productEstimate = productEstimateRepository.findById(estimateId);
        productEstimate.setEstimate_isNameless(1);
        productEstimateRepository.save(productEstimate);
    }

    //查询商品评价数
    @Override
    public long findEstimateCount(long productId) {
        return productEstimateRepository.findCount(productId);
    }
}
