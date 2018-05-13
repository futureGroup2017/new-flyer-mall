package org.wlgzs.xf_mall.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.wlgzs.xf_mall.entity.Activity;
import org.wlgzs.xf_mall.entity.Product;
import org.wlgzs.xf_mall.entity.ProductCategory;
import org.wlgzs.xf_mall.service.ActivityService;
import org.wlgzs.xf_mall.service.ProductService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: 阿杰
 * @Date: 2018/5/9 20:52
 * @Description: 主页
 */
@RequestMapping("HomeController")
@RestController
public class HomeController {
    @Resource
    ProductService productService;
    @Resource
    ActivityService activityService;
    /**
     * @author 阿杰
     * @param [model]
     * @return org.springframework.web.servlet.ModelAndView
     * @description 主页商品数据
     */
    @RequestMapping("/homeProduct")
    public ModelAndView homeProduct(Model model){
        List<ProductCategory> productOneCategories = productService.findProductOneCategoryList();
        model.addAttribute("productOneCategories", productOneCategories);
        List<ProductCategory> productTwoCategories = productService.findProductTwoCategoryList();
        model.addAttribute("productTwoCategories", productTwoCategories);
        List<Activity> activities = activityService.getActivity();
        List<Object> activityPictureList = new ArrayList<Object>();
        for (int i = 0; i < activities.size(); i++) {
            activityPictureList.add(activities.get(i).getActivity_picture());
        }
        model.addAttribute("activities",activities);
        model.addAttribute("activityPictureList",activityPictureList);
        List<Product> products = productService.getProductList();
        model.addAttribute("products",products);
        return new ModelAndView("Index");
    }
}
