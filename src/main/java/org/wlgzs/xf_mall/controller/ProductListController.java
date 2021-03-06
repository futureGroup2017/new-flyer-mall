package org.wlgzs.xf_mall.controller;

import com.google.gson.Gson;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.wlgzs.xf_mall.base.BaseController;
import org.wlgzs.xf_mall.entity.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: 阿杰
 * @Date: 2018/4/24 16:44
 * @Description: 商品  购物车  收藏  搜索
 */
@RequestMapping("ProductListController")
@RestController
public class ProductListController extends BaseController {

    /**
     * @param [model]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 跳转至商品列表页面
     */
    @RequestMapping("/toProductList")
    public ModelAndView toProductList(Model model,HttpServletRequest request ,@RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "limit", defaultValue = "12") int limit) throws IOException {
        //遍历一级二级分类
        List<ProductCategory> productOneCategories = productService.findProductOneCategoryList();
        model.addAttribute("productOneCategories", productOneCategories);
        List<ProductCategory> productTwoCategories = productService.findProductTwoCategoryList();
        model.addAttribute("productTwoCategories", productTwoCategories);
        //推荐商品
        HttpSession session = request.getSession();
        if (session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            long userId = user.getUserId();
            List<Product> recommendedProducts = productService.recommendedByUserId(userId, request);
            model.addAttribute("recommendedProducts", recommendedProducts);
        }
        String product_keywords = "";
        if (page != 0) page--;
        Page<Product> pages = productService.getProductListPage(product_keywords, page, limit);
        model.addAttribute("TotalPages", pages.getTotalPages());//查询的页数
        model.addAttribute("Number", pages.getNumber() + 1);//查询的当前第几页
        List<Product> products = pages.getContent();
        String img;
        for (Product product : products) {
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
                product.setProduct_picture(img);
            }
        }
        model.addAttribute("products", products);//查询的当前页的集合
        return new ModelAndView("productList");
    }

    /**
     * @param [model, id]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 跳转至商品详情页面
     */
    @RequestMapping("/toProduct")
    public ModelAndView toProduct(Model model, long productId, HttpServletRequest request) throws IOException {
        Product product = productService.findProductById(productId);
        long count = ordersService.searchProductCount(productId);
        model.addAttribute("count", count);
        long estimateCount = productEstimateService.findEstimateCount(productId);
        model.addAttribute("estimateCount", estimateCount);
        String[] images = new String[0];
        if (product.getProduct_picture().contains(",")) {
            images = product.getProduct_picture().split(",");
        }
        model.addAttribute("images", images);
        model.addAttribute("product", product);
        List<ProductEstimate> productEstimates = productEstimateService.findEstimateByproductId(productId);
        model.addAttribute("productEstimates", productEstimates);
        HttpSession session = request.getSession();
        if (session.getAttribute("userId") != null) {
            User user = (User) session.getAttribute("user");
            long userId = user.getUserId();
            //添加足迹
            Collection collection = productService.findByCollectionUserIdAndProductId(userId, productId);
            model.addAttribute("collection", collection);
            footprintService.save(request, userId, productId);
            //推荐商品
            List<Product> recommendedProducts = productService.recommendedByUserId(userId, request);
            model.addAttribute("recommendedProducts", recommendedProducts);
        }
        return new ModelAndView("productDetails");
    }

    /**
     * @param [model, userId, productId, request]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 添加购物车
     */
    @RequestMapping("/addShoppingProduct")
    public String addShoppingProduct(long productId, int shoppingCart_count, HttpServletRequest request) {
        long userId = 0;
        HttpSession session = request.getSession();
        if (session.getAttribute("userId") != null) {
            userId = (long) session.getAttribute("userId");
        }
        productService.save(userId, productId, shoppingCart_count, request);
        return "添加成功";
    }

    /**
     * @param [model, userId, productId, request]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 添加收藏
     */
    @RequestMapping("/addCollectionProduct")
    public ModelAndView addCollectionProduct(long productId, HttpServletRequest request) {
        long userId = 0;
        HttpSession session = request.getSession();
        if (session.getAttribute("userId") != null) {
            userId = (long) session.getAttribute("userId");
        }
        productService.saveCollection(userId, productId, request);
        String url = "redirect:/ProductListController/toProduct?productId=" + productId + "&userId=" + userId;
        return new ModelAndView(url);
    }

    /**
     * @param [model, userId]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 跳转至购物车
     */
    @RequestMapping("/shoppingCart")
    public ModelAndView toShoppingCart(Model model, long userId,HttpServletRequest request) throws IOException {
        List<ShoppingCart> shoppingCarts = productService.findByUserIdCart(userId);
        model.addAttribute("shoppingCarts", shoppingCarts);
        //推荐商品
        List<Product> recommendedProducts = productService.recommendedByUserId(userId, request);
        model.addAttribute("recommendedProducts", recommendedProducts);
        return new ModelAndView("shoppingCart");
    }

    /**
     * @param [shoppingCart_id, userId, productId, request]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 购物车移至收藏
     */
    @RequestMapping("/moveToCollectionProduct")
    public ModelAndView moveToCollectionProduct(long shoppingCartId, long userId, long productId, HttpServletRequest request) {
        productService.moveToCollectionProduct(shoppingCartId, userId, productId, request);
        String url = "redirect:/ProductListController/shoppingCart?userId=" + userId;
        return new ModelAndView(url);
    }

    /**
     * @param [shoppingCartId, userId]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 删除购物车
     */
    @RequestMapping("/deleteShoppingProduct")
    public ModelAndView deleteShoppingProduct(long shoppingCartId, long userId) {
        productService.deleteShoppingCart(shoppingCartId);
        String url = "redirect:/ProductListController/shoppingCart?userId=" + userId;
        return new ModelAndView(url);
    }

    /**
     * @param [shoppingCartId]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 批量删除购物车
     */
    @RequestMapping("/deleteShoppingProducts")
    public ModelAndView deleteShoppingProducts(@RequestParam(value = "shoppingCartId", defaultValue = "435,438") String shoppingCartId, long userId) {
        productService.deleteShoppingCarts(shoppingCartId);
        String url = "redirect:/ProductListController/shoppingCart?userId=" + userId;
        return new ModelAndView(url);
    }

    /**
     * @param [shopping_cart_id, shopping_cart_count]
     * @return java.lang.String
     * @author 阿杰
     * @description 修改购物车数量
     */
    @RequestMapping("/changeShoppingCarCount")
    public String changeShoppingCarCount(long shopping_cart_id, int shopping_cart_count) {
        productService.changeShoppingCarCount(shopping_cart_id, shopping_cart_count);
        return "成功";
    }

    /**
     * @param [model, userId, collection_id]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 跳转至用户的收藏
     */
    @RequestMapping("/collectionProduct")
    public ModelAndView toCollection(Model model, long userId,HttpServletRequest request) throws IOException {
        List<Collection> collections = productService.findByUserIdCollection(userId);
        model.addAttribute("collections", collections);
        //推荐商品
        List<Product> recommendedProducts = productService.recommendedByUserId(userId, request);
        model.addAttribute("recommendedProducts", recommendedProducts);
        return new ModelAndView("collection");
    }

    /**
     * @param [collection_id, userId]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 删除收藏
     */
    @RequestMapping("/deleteCollectionProduct")
    public ModelAndView deleteCollectionProduct(long collectionId, long userId) {
        productService.deleteCollection(collectionId);
        String url = "redirect:/ProductListController/collectionProduct?userId=" + userId;
        return new ModelAndView(url);
    }

    /**
     * @param [collectionId]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 批量删除收藏
     */
    @RequestMapping("/deleteCollectionProducts")
    public ModelAndView deleteCollectionProducts(@RequestParam(value = "collectionId", defaultValue = "439,449") String collectionId,HttpServletRequest request) {
        productService.deleteCollections(collectionId);
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        long userId = user.getUserId();
        String url = "redirect:/ProductListController/collectionProduct?userId=" + userId;
        return new ModelAndView(url);
    }

    /**
     * @param [model, product_keywords, userId]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 搜索收藏商品
     */
    @RequestMapping("/findCollectionProducts")
    public ModelAndView findCollectionProducts(Model model, HttpServletRequest request, String product_keywords) throws IOException {
        //推荐商品
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        long userId = user.getUserId();
        List<Collection> collections = productService.findCollections(product_keywords, userId);
        model.addAttribute("collections", collections);
        List<Product> recommendedProducts = productService.recommendedByUserId(userId, request);
        model.addAttribute("recommendedProducts", recommendedProducts);
        String url = "redirect:/ProductListController/collectionProduct?userId=" + userId;
        return new ModelAndView(url);
    }

    /**
     * @param [request, response]
     * @return void
     * @author 阿杰
     * @description 搜索提示
     */
    @RequestMapping("/searchWord")
    public void searchWord(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String category_name = request.getParameter("product_keywords");
        //category_name = "2";
        List<ProductCategory> productCategories = productService.findProductByWord(category_name);
        List<Object> productKeywordList = new ArrayList<>();
        for (ProductCategory productCategory : productCategories) {
            productKeywordList.add(productCategory.getCategory_name());
        }
        Gson gson = new Gson();
        String json = gson.toJson(productKeywordList);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(json);
    }

    /**
     * @param [model, product_keywords, page, limit]
     * @return org.springframework.web.servlet.ModelAndView
     * @author 阿杰
     * @description 前台模糊搜索商品
     */
    @RequestMapping("/searchProductList")
    public ModelAndView searchProductList(Model model, HttpServletRequest request, String product_category, @RequestParam(value = "page", defaultValue = "0") int page,
                                          @RequestParam(value = "limit", defaultValue = "12") int limit) throws IOException {
        if(product_category.length()==0 && product_category.equals("")){
            return new ModelAndView("redirect:/ProductListController/searchCategoryProduct");
        }
        //查询关键字是否为敏感词汇
        if (searchShieldService.querySensitive(product_category)) {
            if (page != 0) page--;
            Page<Product> pages = productService.searchProduct(request, product_category,page,limit);
            //Page<Product> pages = productService.findProductByTwoCategory(product_category, page, limit);
            model.addAttribute("TotalPages", pages.getTotalPages());//查询的页数
            model.addAttribute("Number", pages.getNumber() + 1);//查询的当前第几页
            List<Product> products = pages.getContent();
            String img;
            for (Product product : products) {
                if (product.getProduct_picture().contains(",")) {
                    img = product.getProduct_picture();
                    img = img.substring(0, img.indexOf(","));
                    product.setProduct_picture(img);
                }
            }
            if(products.size()==0){
                model.addAttribute("mag","抱歉您搜索的商品不存在");
            }
            model.addAttribute("products", products);//查询的当前页的集合
            model.addAttribute("product_category", product_category);
            //遍历一级二级分类
            List<ProductCategory> productOneCategories = productService.findProductOneCategoryList();
            model.addAttribute("productOneCategories", productOneCategories);
            List<ProductCategory> productTwoCategories = productService.findProductTwoCategoryList();
            model.addAttribute("productTwoCategories", productTwoCategories);
            //推荐商品
            HttpSession session = request.getSession();
            if (session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                long userId = user.getUserId();
                List<Product> recommendedProducts = productService.recommendedByUserId(userId, request);
                model.addAttribute("recommendedProducts", recommendedProducts);
            }
        } else {
            model.addAttribute("mag", "抱歉您搜索的商品不存在");
        }
        return new ModelAndView("productList");
    }
    /**
     * @author 阿杰
     * @param [model, request, product_category, page, limit]
     * @return org.springframework.web.servlet.ModelAndView
     * @description 按分类搜索
     */
    @RequestMapping("/searchCategoryProduct")
    public ModelAndView searchCategoryProduct(Model model, HttpServletRequest request, String product_category, @RequestParam(value = "page", defaultValue = "0") int page,
                                          @RequestParam(value = "limit", defaultValue = "12") int limit) throws IOException {
        //查询关键字是否为敏感词汇
        if (searchShieldService.querySensitive(product_category)) {
            if (page != 0) page--;
            //Page<Product> pages = productService.searchProduct(request, product_category,page,limit);
            Page<Product> pages = productService.findProductByTwoCategory(product_category, page, limit);
            model.addAttribute("TotalPages", pages.getTotalPages());//查询的页数
            model.addAttribute("Number", pages.getNumber() + 1);//查询的当前第几页
            List<Product> products = pages.getContent();
            String img;
            for (Product product : products) {
                if (product.getProduct_picture().contains(",")) {
                    img = product.getProduct_picture();
                    img = img.substring(0, img.indexOf(","));
                    product.setProduct_picture(img);
                }
            }
            if(products.size()==0){
                model.addAttribute("mag","抱歉您搜索的商品不存在");
            }
            model.addAttribute("products", products);//查询的当前页的集合
            model.addAttribute("product_category", product_category);
            //遍历一级二级分类
            List<ProductCategory> productOneCategories = productService.findProductOneCategoryList();
            model.addAttribute("productOneCategories", productOneCategories);
            List<ProductCategory> productTwoCategories = productService.findProductTwoCategoryList();
            model.addAttribute("productTwoCategories", productTwoCategories);
            //推荐商品
            HttpSession session = request.getSession();
            if (session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                long userId = user.getUserId();
                List<Product> recommendedProducts = productService.recommendedByUserId(userId, request);
                model.addAttribute("recommendedProducts", recommendedProducts);
            }
        } else {
            model.addAttribute("mag", "抱歉您搜索的商品不存在");
        }
        return new ModelAndView("searchCategoryProduct");
    }

    //积分商品展示
    @RequestMapping("/integralProduct")
    public ModelAndView findByProduct_isRedeemable(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "limit", defaultValue = "10") int limit) {
        if (page != 0) page--;
        int product_isRedeemable = 1;
        Page pages = productService.findByProduct_isRedeemable(product_isRedeemable, page, limit);
        model.addAttribute("TotalPages", pages.getTotalPages());//查询的页数
        model.addAttribute("Number", pages.getNumber() + 1);//查询的当前第几页
        List<Product> products = pages.getContent();
        String img;
        for (Product product : products) {
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
                product.setProduct_picture(img);
            }
        }
        model.addAttribute("products", products);//查询的当前页的集合
        return new ModelAndView("integralProduct");
    }

    //按价格区间划分商品
    @RequestMapping("/findByPrice")
    public ModelAndView findByPrice(Model model,HttpServletRequest request ,String product_mallMinPrice,String product_mallMaxPrice ,@RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "limit", defaultValue = "12") int limit) throws IOException {
        //遍历一级二级分类
        List<ProductCategory> productOneCategories = productService.findProductOneCategoryList();
        model.addAttribute("productOneCategories", productOneCategories);
        List<ProductCategory> productTwoCategories = productService.findProductTwoCategoryList();
        model.addAttribute("productTwoCategories", productTwoCategories);
        //推荐商品
        HttpSession session = request.getSession();
        if (session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            long userId = user.getUserId();
            List<Product> recommendedProducts = productService.recommendedByUserId(userId, request);
            model.addAttribute("recommendedProducts", recommendedProducts);
        }
        if (page != 0) page--;
        Page<Product> pages = productService.findByPrice(product_mallMinPrice,product_mallMaxPrice,page,limit);
        model.addAttribute("TotalPages", pages.getTotalPages());//查询的页数
        model.addAttribute("Number", pages.getNumber() + 1);//查询的当前第几页
        List<Product> products = pages.getContent();
        String img;
        for (Product product : products) {
            if (product.getProduct_picture().contains(",")) {
                img = product.getProduct_picture();
                img = img.substring(0, img.indexOf(","));
                product.setProduct_picture(img);
            }
        }
        if(products.size()==0){
            model.addAttribute("mag","抱歉您搜索的商品不存在");
        }
        model.addAttribute("product_mallMinPrice", product_mallMinPrice);
        model.addAttribute("product_mallMaxPrice", product_mallMaxPrice);
        model.addAttribute("products", products);//查询的当前页的集合
        return new ModelAndView("productPriceList");
    }
}
