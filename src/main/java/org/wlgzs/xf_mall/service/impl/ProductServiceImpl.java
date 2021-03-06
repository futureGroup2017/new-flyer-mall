package org.wlgzs.xf_mall.service.impl;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wlgzs.xf_mall.dao.*;
import org.wlgzs.xf_mall.entity.Collection;
import org.wlgzs.xf_mall.entity.*;
import org.wlgzs.xf_mall.service.ProductService;
import org.wlgzs.xf_mall.util.IdsUtil;
import org.wlgzs.xf_mall.util.PageUtil;
import org.wlgzs.xf_mall.util.PageUtilTwo;
import org.wlgzs.xf_mall.util.ReadFiles;

import javax.annotation.Resource;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @Auther: 阿杰
 * @Date: 2018/4/18 21:21
 * @Description:
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductRepository productRepository;
    @Resource
    private ProductCategoryRepository productCategoryRepository;
    @Resource
    private ShoppingCartRepository shoppingCartRepository;
    @Resource
    private CollectionRepository collectionRepository;
    @Resource
    FootprintRepository footprintRepository;
    @Resource
    private OrdersRepository ordersRepository;

    //分页遍历商品  搜索商品
    @Override
    public Page<Product> getProductListPage(String product_keywords, int page, int limit) {
        Sort sort = new Sort(Sort.Direction.DESC, "productId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        Specification<Product> specification = new PageUtil<Product>(product_keywords).getPage("product_keywords", "product_category");
        return productRepository.findAll(specification, pageable);
    }

    //添加商品
    @Override
    public void saveProduct(String product_details, MultipartFile[] myFileNames, HttpSession session, HttpServletRequest request) {
        String realName;
        String realPath = "";
        String[] str = new String[myFileNames.length];
        for (int i = 0; i < myFileNames.length; i++) {
            if (!Objects.equals(myFileNames[i].getOriginalFilename(), "")) {
                String fileName = myFileNames[i].getOriginalFilename();
                assert fileName != null;
                String fileNameExtension = fileName.substring(fileName.indexOf("."));
                // 生成实际存储的真实文件名
                realName = UUID.randomUUID().toString() + fileNameExtension;
                realPath = "/upload/product/" + realName;
                File uploadFile = new File("." + realPath);
                IdsUtil.writeFile(myFileNames[i], uploadFile);
            }
            if (!Objects.equals(myFileNames[i].getOriginalFilename(), "")) {
                str[i] = "/api/path" + realPath;
            }
        }
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < str.length; i++) {
            if (!Objects.equals(myFileNames[i].getOriginalFilename(), "")) {
                stringBuffer.append(str[i]).append(",");
            }
        }
        String product_picture = stringBuffer.substring(0, stringBuffer.length() - 1);
        Map<String, String[]> properties = request.getParameterMap();
        Product product = new Product();
        try {
            BeanUtils.populate(product, properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        product.setProduct_details(product_details);
        product.setProduct_picture(product_picture);
        productRepository.save(product);
    }

    //富文本添加图片
    @Override
    public String[] uploadImg(MultipartFile myFileName, HttpSession session, HttpServletRequest request) {
        String realName;
        String realPath = "";
        if (myFileName != null && myFileName.getOriginalFilename()!=null) {
            String fileName = myFileName.getOriginalFilename();
            String fileNameExtension = fileName.substring(fileName.indexOf("."));
            // 生成实际存储的真实文件名
            realName = "fuwenben" + UUID.randomUUID().toString() + fileNameExtension;
            realPath = "/upload/product/" + realName;
            File uploadFile = new File("." + realPath);
            IdsUtil.writeFile(myFileName, uploadFile);
        }
        return new String[]{"/api/path" + realPath};
    }

    //删除商品
    @Override
    public String delete(long productId, HttpServletRequest request) {
        Product product = productRepository.findById(productId);
        if (product != null) {
            String image = product.getProduct_picture();
            String[] img = image.split(",");
            for (String anImg : img) {
                new IdsUtil().deleteFile(anImg.substring(9));
            }
            productRepository.deleteById(productId);
            return "成功";
        } else {
            return "失败";
        }

    }

    //通过id查找商品
    @Override
    public Product findProductById(long productId) {
        return productRepository.findById(productId);
    }

    //通过id查询商品  返回集合
    @Override
    public List<Product> findProductListById(long productId) {
        List<Product> products = productRepository.findByIdReturnOne(productId);
        String img;
        for (Product product : products) {
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
                product.setProduct_picture(img);
            }
        }
        return products;
    }

    /**
     * @param
     * @return java.util.List<org.wlgzs.xf_mall.entity.Product>
     * @author 阿杰
     * @description 结算商品
     */
    @Override
    public List<ShoppingCart> findAllByIds(long[] Ids) {
        List<ShoppingCart> shoppingCarts = shoppingCartRepository.findAllByIds(Ids);
        String img;
        for (ShoppingCart shoppingCart : shoppingCarts) {
            if (shoppingCart.getProduct_picture().contains(",")) {
                img = shoppingCart.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
                shoppingCart.setProduct_picture(img);
            }
        }
        return shoppingCarts;
    }

    //修改商品
    @Override
    public String edit(long productId, String product_details, MultipartFile[] myFileNames, HttpSession session,
                       HttpServletRequest request) {
        Product product = productRepository.findById(productId);
        if (product != null) {
            String realName = "";
            String realPath = "";
            String[] str = new String[myFileNames.length];
            for (int i = 0; i < myFileNames.length; i++) {
                if (!"".equals(myFileNames[i].getOriginalFilename())) {
                    String fileName = myFileNames[i].getOriginalFilename();
                    assert fileName != null;
                    String fileNameExtension = fileName.substring(fileName.indexOf("."));
                    // 生成实际存储的真实文件名
                    realName = UUID.randomUUID().toString() + fileNameExtension;
                    realPath = "/upload/product/" + realName;
                    File uploadFile = new File("." + realPath);
                    IdsUtil.writeFile(myFileNames[i], uploadFile);
                }
                if (!Objects.equals(myFileNames[i].getOriginalFilename(), "")) {
                    str[i] = "/api/path" + realPath;
                }
            }
            StringBuilder stringBuffer = new StringBuilder();
            for (int i = 0; i < str.length; i++) {
                if (!Objects.equals(myFileNames[i].getOriginalFilename(), "")) {
                    stringBuffer.append(str[i]).append(",");
                }
            }
            String product_picture = stringBuffer.substring(0, stringBuffer.length() - 1);
            Map<String, String[]> properties = request.getParameterMap();
            try {
                BeanUtils.populate(product, properties);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            product.setProduct_details(product_details);
            product.setProduct_picture(product_picture);
            productRepository.save(product);
            return "修改成功";
        } else {
            return "修改失败";
        }
    }

    //遍历所有分类  搜索分类
    @Override
    public Page<ProductCategory> getProductCategoryList(String category_name, int page, int limit) {
        Sort sort = new Sort(Sort.Direction.DESC, "categoryId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        Specification<ProductCategory> specification = new PageUtil<ProductCategory>(category_name).getPage("category_name", "parent_name");
        return productCategoryRepository.findAll(specification, pageable);
    }

    //遍历一级分类  不分页
    @Override
    public List<ProductCategory> findProductOneCategoryList() {
        return productCategoryRepository.findOneAll();
    }

    //遍历二级分类  不分页
    @Override
    public List<ProductCategory> findProductTwoCategoryList() {
        return productCategoryRepository.findTwoAll();
    }

    //通过一级分类查找分类名
    @Override
    public List<ProductCategory> findProductByOneCategory(String category) {
        return productCategoryRepository.findByCategoryParentName(category);
    }

    //通过二级分类查找商品
    @Override
    public List<Product> productByOneCategory(String category_name) {
        List<ProductCategory> productCategories = productCategoryRepository.findByCategoryParentNameAndTwo(category_name);
        ProductCategory[] toBeStored = productCategories.toArray(new ProductCategory[productCategories.size()]);
        String[] str = new String[toBeStored.length];
        for (int i = 0; i < toBeStored.length; i++) {
            str[i] = toBeStored[i].getCategory_name();
        }
        return productRepository.findProductByTwoCategory(str);//查询的当前页商品的集合
    }

    //通过二级分类查找商品  分页
    @Override
    public Page<Product> findProductByTwoCategory(String product_category, int page, int limit) {
        Sort sort = new Sort(Sort.Direction.DESC, "productId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        //商品分类，活动，关键字，服务类型三种方式
        Specification<Product> specification = new PageUtil<Product>(product_category).getPage("product_category", "product_serviceType", "product_activity", "product_keywords");
        return productRepository.findAll(specification, pageable);
    }

    //通过多条件查询商品    分页
    //通过二级分类查找商品  不分页
    @Override
    public List<Product> findProductByCategory(String product_category) {
        return productRepository.findByCategory(product_category);
    }

    //增加一级分类
    @Override
    public void saveOne(ProductCategory productCategory, MultipartFile myFileName, HttpSession session, HttpServletRequest request) {
        String realName = "";
        String realPath = "";
        if (myFileName != null) {
            String fileName = myFileName.getOriginalFilename();
            assert fileName != null;
            String fileNameExtension = fileName.substring(fileName.indexOf("."), fileName.length());
            // 生成实际存储的真实文件名
            realName = UUID.randomUUID().toString() + fileNameExtension;
            realPath = "/upload/category/" + realName;
            File uploadFile = new File("." + realPath);
            IdsUtil.writeFile(myFileName, uploadFile);
        }
        productCategory.setCategory_img("/api/path" + realPath);
        productCategory.setParent_name("0");
        productCategoryRepository.save(productCategory);
    }

    //增加二级分类
    @Override
    public void save(ProductCategory productCategory) {
        productCategory.setCategory_show(0);
        productCategory.setCategory_img("0");
        productCategoryRepository.save(productCategory);
    }

    //添加二级分类配件
    @Override
    public void saveTwo(ProductCategory productCategory) {
        productCategory.setCategory_show(2);
        productCategory.setCategory_img("0");
        productCategoryRepository.save(productCategory);
    }

    //删除分类
    @Override
    public String deleteCategory(long categoryId) {
        ProductCategory productCategory = productCategoryRepository.findById(categoryId);
        if (productCategory != null) {
            if (productCategory.getParent_name().equals("0")) {
                List<ProductCategory> productCategories = productCategoryRepository.findByCategoryParentName(productCategory.getCategory_name());
                if (productCategories.size() != 0) {
                    long[] Ids = new long[productCategories.size()];
                    for (int i = 0; i < Ids.length; i++) {
                        Ids[i] = productCategories.get(i).getCategoryId();
                    }
                    productCategoryRepository.deleteByIds(Ids);
                }
                new IdsUtil().deleteFile(productCategory.getCategory_img().substring(9));
            }
            productCategoryRepository.deleteById(categoryId);
            return "删除成功";
        }
        return "删除失败";
    }

    //修改分类
    @Override
    public void editCategory(long categoryId, MultipartFile myFileName, HttpSession session, HttpServletRequest request) {
        ProductCategory productCategory = productCategoryRepository.findById(categoryId);
        productCategory.setCategory_name(request.getParameter("category_name"));
        if (!productCategory.getParent_name().equals("0")) {
            productCategory.setParent_name(request.getParameter("parent_name"));
            if (productCategory.getCategory_show() == 2) {
                productCategory.setCategory_show(2);
                productCategory.setCategory_img("0");
            }
            if (productCategory.getCategory_show() != 2) {
                productCategory.setCategory_show(0);
                productCategory.setCategory_img("0");
            }
        }
        if (productCategory.getParent_name().equals("0")) {
            String realName = "";
            String realPath = "";
            if (myFileName != null) {
                String fileName = myFileName.getOriginalFilename();
                assert fileName != null;
                String fileNameExtension = fileName.substring(fileName.indexOf("."));
                // 生成实际存储的真实文件名
                realName = UUID.randomUUID().toString() + fileNameExtension;
                realPath = "/upload/category/" + realName;
                File uploadFile = new File("." + realPath);
                IdsUtil.writeFile(myFileName, uploadFile);
            }
            new IdsUtil().deleteFile(productCategory.getCategory_img().substring(9));
            productCategory.setCategory_img("/api/path" + realPath);
            productCategory.setCategory_show(Integer.parseInt(request.getParameter("category_show")));
            productCategory.setParent_name("0");
        }
        productCategoryRepository.save(productCategory);
    }

    //按id查找类别
    @Override
    public ProductCategory findProductCategoryById(long categoryId) {
        return productCategoryRepository.findById(categoryId);
    }

    //添加购物车
    @Override
    public void save(long userId, long productId, int shoppingCart_count, HttpServletRequest request) {
        Product product = productRepository.findById(productId);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setShoppingCart_count(shoppingCart_count);
        ShoppingCart findShoppingCart = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (findShoppingCart != null) {
            findShoppingCart.setShoppingCart_count(findShoppingCart.getShoppingCart_count() + shoppingCart.getShoppingCart_count());
            shoppingCartRepository.save(findShoppingCart);
        }
        if (findShoppingCart == null) {
            shoppingCart.setProductId(productId);
            String img = null;
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
            }
            shoppingCart.setProduct_picture(img);
            shoppingCart.setProduct_counterPrice(product.getProduct_counterPrice());
            shoppingCart.setProduct_keywords(product.getProduct_keywords());
            shoppingCart.setProduct_mallPrice(product.getProduct_mallPrice());
            shoppingCart.setProduct_specification(product.getProduct_specification());
            shoppingCart.setUserId(userId);
            shoppingCartRepository.save(shoppingCart);
        }
    }

    //添加收藏
    @Override
    public void saveCollection(long userId, long productId, HttpServletRequest request) {
        Product product = productRepository.findById(productId);

        Collection collection = new Collection();
        Collection findCollection = collectionRepository.findByCollectionUserIdAndProductId(userId, productId);
        if (findCollection != null) {
            collectionRepository.deleteById(findCollection.getCollectionId());
            collectionRepository.save(findCollection);
        }
        if (findCollection == null) {
            collection.setProductId(productId);
            String img = null;
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
            }
            collection.setProduct_picture(img);
            collection.setProduct_keywords(product.getProduct_keywords());
            collection.setProduct_mallPrice(product.getProduct_mallPrice());
            collection.setUserId(userId);
            collectionRepository.save(collection);
        }
    }

    //查找用户收藏是否存在
    @Override
    public Collection findByCollectionUserIdAndProductId(long userId, long productId) {
        return collectionRepository.findByCollectionUserIdAndProductId(userId, productId);
    }

    //用户的购物车
    @Override
    public List<ShoppingCart> findByUserIdCart(long userId) {
        return shoppingCartRepository.findByUserIdCart(userId);
    }

    //购物车移至收藏
    @Override
    public void moveToCollectionProduct(long shoppingCartId, long userId, long productId, HttpServletRequest request) {
        shoppingCartRepository.deleteById(shoppingCartId);
        Product product = productRepository.findById(productId);

        Map<String, String[]> properties = request.getParameterMap();
        Collection collection = new Collection();
        try {
            BeanUtils.populate(collection, properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        Collection findCollection = collectionRepository.findByCollectionUserIdAndProductId(userId, productId);
        if (findCollection != null) {
            collectionRepository.deleteById(findCollection.getCollectionId());
            collectionRepository.save(findCollection);
        }
        if (findCollection == null) {
            collection.setProductId(productId);
            String img = null;
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
            }
            collection.setProduct_picture(img);
            collection.setProduct_keywords(product.getProduct_keywords());
            collection.setProduct_mallPrice(product.getProduct_mallPrice());
            collection.setUserId(userId);
            collectionRepository.save(collection);
        }
    }

    //删除购物车
    @Override
    public void deleteShoppingCart(long shoppingCartId) {
        shoppingCartRepository.deleteById(shoppingCartId);
    }

    //批量删除购物车
    @Override
    public void deleteShoppingCarts(String shoppingCartIds) {
        long[] Ids = IdsUtil.IdsUtils(shoppingCartIds);
        shoppingCartRepository.deleteByIds(Ids);
    }

    //修改购物车数量
    @Override
    public void changeShoppingCarCount(long shopping_cart_id, int shopping_cart_count) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(shopping_cart_id);
        shoppingCart.setShoppingCart_count(shopping_cart_count);
        shoppingCartRepository.save(shoppingCart);
    }

    //用户的收藏
    @Override
    public List<Collection> findByUserIdCollection(long userId) {
        return collectionRepository.findByUserIdCollection(userId);
    }

    //删除收藏
    @Override
    public void deleteCollection(long collectionId) {
        collectionRepository.deleteById(collectionId);
    }

    //批量删除收藏
    @Override
    public void deleteCollections(String collectionId) {
        long[] Ids = IdsUtil.IdsUtils(collectionId);
        collectionRepository.deleteByIds(Ids);
    }

    //搜索收藏商品
    @Override
    public List<Collection> findCollections(String product_keywords, long userId) {
        return collectionRepository.findCollections(product_keywords, userId);
    }

    //搜索提示
    @Override
    public List<ProductCategory> findProductByWord(String category_name) {
        return productCategoryRepository.findProductByWord(category_name);
    }

    //积分商品展示
    @Override
    public Page<Product> findByProduct_isRedeemable(int product_isRedeemable, int page, int limit) {
        Sort sort = new Sort(Sort.Direction.ASC, "productId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        Specification<Product> specification = (Specification<Product>) (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("product_isRedeemable"), product_isRedeemable);
            return criteriaBuilder.and(predicate);
        };
        return productRepository.findAll(specification, pageable);
    }

    //商品推荐
    @Override
    public List<Product> recommendedByUserId(long userId, HttpServletRequest request) throws IOException {
        //通过用户id查询最新足迹商品
        List<Footprint> footprints = footprintRepository.recommendedByUserId(userId);
        List<Product> products = new ArrayList<>();
        if (footprints != null && footprints.size() != 0) {
            List<String> stringListOne = new ArrayList<>();
            for (Footprint footprint : footprints) {
                //String fileName = footprint.getProduct_keywords();
                // 生成实际存储的真实文件名
                String realName = UUID.randomUUID().toString();
                String path = "./upload/txtFile/" + realName;
                File filePath = new File(path + "/" + realName + ".txt");
                if (!filePath.getParentFile().exists()) filePath.getParentFile().mkdirs();
                IdsUtil.saveFile(footprint.getProduct_keywords(), filePath);
                //读取文件
                ReadFiles.NormalTFOfAll(path);
                Map<String, Float> idf = ReadFiles.idf();
                stringListOne.addAll(idf.keySet());
                System.out.println(stringListOne);
                if (filePath.exists() && filePath.isFile()) {
                    filePath.delete();
                }
                File fileTwo = new File(path);
                if (fileTwo.exists() && fileTwo.isDirectory()) {
                    fileTwo.delete();
                }
            }
            //去重 利用set顺序不变
            Set<String> set = new HashSet<>();
            List<String> stringList = new ArrayList<String>();
            for (String cd : stringListOne) {
                if (set.add(cd)) {
                    stringList.add(cd);
                }
            }
            String[] product_category = stringList.toArray(new String[stringList.size()]);
            for (int i = 0; i < product_category.length; i++) {
                if (product_category[i].equals("新") || product_category[i].equals("飞") || product_category[i].equals("新飞") || product_category[i].equals("40/") || product_category[i].equals("43/50") || product_category[i].equals("双") || product_category[i].equals("8/6") || product_category[i].equals("6.8kg/7.5") || product_category[i].equals("家用") || product_category[i].equals("丰包邮") || product_category[i].equals("飞8公斤")) {
                    product_category[i] = product_category[product_category.length - 1];
                    product_category = Arrays.copyOf(product_category, product_category.length - 1);
                    i--;
                }
            }
            //通过关键词查询商品
            Sort sort = new Sort(Sort.Direction.DESC, "productId");
            PageUtilTwo<Product> pageUtilTwo = new PageUtilTwo<>();
            Specification<Product> specification = pageUtilTwo.getPage(product_category);
            products = productRepository.findAll(specification, sort);
            //查询用户的订单
            List<Orders> orders = ordersRepository.userOrderList(userId);
            if (orders != null && orders.size() != 0) {
                Date data = new Date();
                //将近半年的订单放在一个集合中
                List<Orders> ordersList = new ArrayList<>();
                for (Orders order : orders) {
                    double between = (double) (data.getTime() - order.getOrder_purchaseTime().getTime()) / (double) (1000 * 60 * 60 * 24);
                    if (between < 180) {
                        ordersList.add(order);
                    }
                }
                //将近半年订单的商品id放在一个数组里
                long[] orderProductId = new long[ordersList.size()];
                for (int i = 0; i < ordersList.size(); i++) {
                    double between = (double) (data.getTime() - orders.get(i).getOrder_purchaseTime().getTime()) / (double) (1000 * 60 * 60 * 24);
                    if (between < 180) {
                        orderProductId[i] = ordersList.get(i).getProductId();
                    }
                }
                //将订单中存在的商品去除
                Iterator<Product> it = products.iterator(); // 迭代器,对集合ArrayList中的元素进行取出
                for (long anOrderProductId : orderProductId) {
                    while (it.hasNext()) { // 调用方法hasNext()判断集合中是否有元素
                        Product x = it.next();
                        if (x.getProductId() == anOrderProductId) {
                            // 调用方法next()取出集合中的元素
                            it.remove();
                        }
                    }
                }
            }
            String img;
            for (Product product : products) {
                if (product.getProduct_picture().contains(",")) {
                    img = product.getProduct_picture();
                    img = img.substring(0, img.indexOf(","));
                    product.setProduct_picture(img);
                }
            }
        }
        return products;
    }

    //主页商品数据
    @Override
    public List<Product> homeProductList(List<ProductCategory> productOneCategories) {
        List<Product> productsOne = productOneCategory(productOneCategories.get(0).getCategory_name());//主页部分第一分类商品
        for (Product aProductsOne : productsOne) {
            aProductsOne.setProduct_category(productOneCategories.get(0).getCategory_name());
        }
        List<Product> products = new ArrayList<>(productsOne);
        List<Product> productsTwo = productOneCategory(productOneCategories.get(1).getCategory_name());//主页部分第二分类商品
        for (Product aProductsTwo : productsTwo) {
            aProductsTwo.setProduct_category(productOneCategories.get(1).getCategory_name());
        }
        products.addAll(productsTwo);
        List<Product> productsThree = productOneCategory(productOneCategories.get(2).getCategory_name());//主页部分第三分类商品
        for (Product aProductsThree : productsThree) {
            aProductsThree.setProduct_category(productOneCategories.get(2).getCategory_name());
        }
        products.addAll(productsThree);
        List<Product> productsFour = productOneCategory(productOneCategories.get(3).getCategory_name());//主页部分第四分类商品
        for (Product aProductsFour : productsFour) {
            aProductsFour.setProduct_category(productOneCategories.get(3).getCategory_name());
        }
        products.addAll(productsFour);
        List<Product> productsFive = productOneCategory(productOneCategories.get(4).getCategory_name());//主页部分第五分类商品
        for (Product aProductsFive : productsFive) {
            aProductsFive.setProduct_category(productOneCategories.get(4).getCategory_name());
        }
        products.addAll(productsFive);
        String img;
        for (Product product : products) {
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
                product.setProduct_picture(img);
            }
        }
        return products;
    }

    //主页最新商品
    @Override
    public List<Product> getProductList() {
        Sort sort = new Sort(Sort.Direction.DESC, "productId");
        return productRepository.getProductList(sort);
    }

    //搜索商品
    @Override
    public Page<Product> searchProduct(HttpServletRequest request, String product_category, int page, int limit) throws IOException {
        // 生成实际存储的真实文件名
        String realName = UUID.randomUUID().toString();
        String path = "./upload/txtFile/" + realName;
        File filePath = new File(path + "/" + realName + ".txt");
        if (!filePath.getParentFile().exists()) filePath.getParentFile().mkdirs();
        IdsUtil.saveFile(product_category, filePath);
        //读取搜索文件
        ReadFiles.NormalTFOfAll(path);
        Map<String, Float> idf = ReadFiles.idf();
        List<String> stringList = new ArrayList<>(idf.keySet());
        System.out.println(stringList);
        if (filePath.exists() && filePath.isFile()) {
            filePath.delete();
        }
        File fileTwo = new File(path);
        if (fileTwo.exists() && fileTwo.isDirectory()) {
            fileTwo.delete();
        }
        String[] product_categories = stringList.toArray(new String[stringList.size()]);
        for (int i = 0; i < product_categories.length; i++) {
            if (product_categories[i].equals("新") || product_categories[i].equals("飞") || product_categories[i].equals("新飞") || product_categories[i].equals("40/") || product_categories[i].equals("43/50") || product_categories[i].equals("双") || product_categories[i].equals("8/6") || product_categories[i].equals("6.8kg/7.5") || product_categories[i].equals("家用") || product_categories[i].equals("丰包邮") || product_categories[i].equals("飞8公斤")) {
                product_categories[i] = product_categories[product_categories.length - 1];
                product_categories = Arrays.copyOf(product_categories, product_categories.length - 1);
                i--;
            }
        }
        //通过关键词查询商品
        Sort sort = new Sort(Sort.Direction.DESC, "productId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        /*List<Product> productList = new ArrayList<>();
        for (int i = 0; i < product_categories.length; i++) {
            productList.addAll(productRepository.findProductByProductKeywords(product_categories[i]));
        }*/
        PageUtilTwo<Product> pageUtilTwo = new PageUtilTwo<>();
        Specification<Product> specification = pageUtilTwo.getPage(product_categories);
        //Page<Product> pages = new PageImpl<>(productList,pageable,productList.size());
        return productRepository.findAll(specification, pageable);
    }

    private List<Product> productOneCategory(String category_name) {
        List<ProductCategory> productCategories = productCategoryRepository.findByCategoryParentNameAndTwo(category_name);
        ProductCategory[] toBeStored = productCategories.toArray(new ProductCategory[productCategories.size()]);
        String[] str = new String[toBeStored.length];
        for (int i = 0; i < toBeStored.length; i++) {
            str[i] = toBeStored[i].getCategory_name();
        }
        return productRepository.findProductByTwoCategory(str);//查询的当前页商品的集合
    }

    @Override
    public Page<Product> findByPrice(String product_mallMinPrice, String product_mallMaxPrice, int page, int limit) {

        Sort sort = new Sort(Sort.Direction.DESC, "productId");
        Pageable pageable = PageRequest.of(page, limit, sort);
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = null;
                Path path = root.get("product_mallPrice");
                if((product_mallMinPrice != null && !product_mallMinPrice.equals("")) || (product_mallMaxPrice != null && !product_mallMaxPrice.equals(""))) {
                    if (product_mallMinPrice != null && !product_mallMinPrice.equals("") && product_mallMaxPrice != null && !product_mallMaxPrice.equals("") ) {
                        float a;
                        float minPrice = Integer.valueOf(product_mallMinPrice);
                        float maxPrice = Integer.valueOf(product_mallMaxPrice);
                        if (minPrice > maxPrice) {
                            a = minPrice;
                            minPrice = maxPrice;
                            maxPrice = a;
                        }
                        predicate = criteriaBuilder.and(
                                criteriaBuilder.ge(path, minPrice),
                                criteriaBuilder.le(path, maxPrice)
                        );
                    } else if (product_mallMinPrice != null) {
                        float minPrice = Integer.valueOf(product_mallMinPrice);
                        predicate = criteriaBuilder.lt(path, minPrice);
                    } else {
                        float maxPrice = Integer.valueOf(product_mallMaxPrice);
                        predicate = criteriaBuilder.gt(path, maxPrice);
                    }
                }
                return predicate;
            }
        };
        return productRepository.findAll(specification, pageable);
    }

    @Override
    public void adminDeleteProducts(String productId) {
        long[] ids = IdsUtil.IdsUtils(productId);
        productRepository.deleteByIds(ids);
    }
}
