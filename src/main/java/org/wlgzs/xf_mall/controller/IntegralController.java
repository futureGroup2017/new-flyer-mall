package org.wlgzs.xf_mall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wlgzs.xf_mall.base.BaseController;
import org.wlgzs.xf_mall.entity.User;
import org.wlgzs.xf_mall.entity.UserIntegral;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Auther: 李晓珊
 * @Date: 2018/4/21 22:11
 * @Description:积分管理
 */
@Controller
public class IntegralController extends BaseController {

    //遍历用户积分明细
    @RequestMapping("/IntegralController/integralList")
    public String integralList(Model model,long userId) {
        List<UserIntegral> integralLists = userIntegralService.getUserIntegral(userId);
        model.addAttribute("integralLists", integralLists);
        User user = userService.findUserById(userId);
        model.addAttribute("user",user);
        return "userIntegralList";
    }

    //遍历用户收入积分
    @RequestMapping("/IntegralController/UserIntegralIncome")
    public String incomeList(Model model,long userId) {
        List<UserIntegral> integralLists = userIntegralService.getUserIntegralIncome(userId);
        model.addAttribute("integralLists", integralLists);
        return "userIntegralList";
    }

    //遍历用户支出积分
    @RequestMapping("/IntegralController/UserIntegralExpend")
    public String expendList(Model model,long userId) {
        List<UserIntegral> integralLists = userIntegralService.getUserIntegralExpend(userId);
        model.addAttribute("integralLists", integralLists);
        return "userIntegralList";
    }

    //在购买的同时添加积分明细
    @RequestMapping("/IntegralController/addUserIntegral")
    public void AddUserIntegral(long userId,HttpServletRequest request) {
        userIntegralService.save(userId,request);
    }

    //删除积分记录
    @RequestMapping("/UserIntegralController/deleteUserIntegral")
    public String delete(long userIntegralId,long userId) {
        userIntegralService.delete(userIntegralId);
        return "redirect:/IntegralController/integralList?userId=" + userId;
    }
}
