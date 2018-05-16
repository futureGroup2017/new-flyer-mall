package org.wlgzs.xf_mall.controller;

import org.apache.commons.beanutils.BeanUtils;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import org.wlgzs.xf_mall.entity.ShippingAddress;
import org.wlgzs.xf_mall.entity.User;
import org.wlgzs.xf_mall.service.LogUserService;
import org.wlgzs.xf_mall.service.ShippingAddressService;
import org.wlgzs.xf_mall.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author:胡亚星
 * @createTime 2018-04-22 16:18
 * @description:用户信息修改控制层
 **/
@RequestMapping("UserManagementController")
@RestController
public class UserManagementController {

    @Resource
    UserService userService;

    @Resource
    LogUserService logUserService;

    @Resource
    ShippingAddressService shippingAddressService;
    

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/4/23 21:29
     * @Description:展示用户信息
     */
    @RequestMapping("information")
    public ModelAndView displayInformation(Model model, Long userId) {
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        return new ModelAndView("information");
    }

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/1 8:16
     * @Description:修改用户名(个人信息展示页面)
     */
    @RequestMapping("changeInformation")
    public ModelAndView ModifyName(Model model, HttpServletRequest request,Long userId) {
        Map<String, String[]> properties = request.getParameterMap();
        System.out.println(userId);
        User user = userService.findUserById(userId);
        System.out.println(user);
        try {
            BeanUtils.populate(user, properties);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        String user_name1 = user.getUser_name();
        String user_name = request.getParameter("user_name");
        //判断用户名是否存在
        if(logUserService.selectName(user_name)){
            user.setUser_name(user_name);
            userService.edit(user);
            //修改收货地址表中的用户名
            shippingAddressService.modifyName(user_name,user_name1);
            //从新存入session
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(60 * 20);
            session.setAttribute("name", user.getUser_name());//之后用过滤器实现
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("user",user);
        }
        model.addAttribute("user", user);
        return new ModelAndView("information");
    }


    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/1 8:11
     * @Description:点击用户头像跳转到头像修改页面
     */
    @RequestMapping("toModifyAvatar")
    public ModelAndView toModifyAvatar(Model model, Long userId) {
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        return new ModelAndView("ModifyAvatar");
    }


    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/4/23 21:28
     * @Description:实现用户头像的修改 文件上传，替换原来的地址
     */
    @RequestMapping("/ModifyAvatar")
    public ModelAndView add(@RequestParam("file") MultipartFile myFileName, HttpSession session,
                            Model model, HttpServletRequest request) throws IOException {
        String userId = request.getParameter("userId");
        long id = Long.parseLong(userId);
        String realName = "";
        String user_avatar = "";
        if (!myFileName.getOriginalFilename().equals("")) {
            String fileName = myFileName.getOriginalFilename();
            String fileNameExtension = fileName.substring(fileName.indexOf("."), fileName.length());

            // 生成实际存储的真实文件名
            realName = UUID.randomUUID().toString() + fileNameExtension;

            // "/upload"是你自己定义的上传目录
            String realPath = session.getServletContext().getRealPath("/headPortrait");
            File uploadFile = new File(realPath, realName);
            myFileName.transferTo(uploadFile);
            user_avatar = request.getContextPath() + "/headPortrait/" + realName;
        } else {
            user_avatar = request.getContextPath() + "/headPortrait/" + "morende.jpg";
        }
        System.out.println(user_avatar);
        Map<String, String[]> properties = request.getParameterMap();
        User user = userService.findUserById(id);
        try {
            BeanUtils.populate(user, properties);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        user.setUser_avatar(user_avatar);
        userService.modifyAvatar(user_avatar, id);
        model.addAttribute("user", user);
        return new ModelAndView("information");
    }

    /**
     * @author 胡亚星
     * @date 2018/5/8 21:55
     * @param
     * @return
     *@Description:跳转到安全设置
     */
    @RequestMapping("toSafety")
    public ModelAndView toSafety(Model model, Long userId){
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        return new ModelAndView("safety");
    }

    /**
     * @author 胡亚星
     * @date 2018/5/9 17:51
     * @param
     * @return
     *@Description:跳转到修改邮箱
     */
    @RequestMapping("toChangeEmail")
    public ModelAndView toChangeEmail(){
        System.out.println("123123");
        return new ModelAndView("changeEmail");
    }

    /**     
     * @author 胡亚星
     * @date 2018/5/9 17:54  
     * @param   
     * @return   
     *@Description:实现修改邮箱
     */
    @RequestMapping("changeEmail")
    public ModelAndView changeEmail(Model model,String user_mail,Long userId){
        //检查邮箱是否存在
        if(logUserService.selectEmail(user_mail)){//可以继续
            model.addAttribute("mgs", "修改成功");
            System.out.println("user_mail=============="+user_mail);
            userService.changeEmail(user_mail,userId);//修改
            User user = userService.findUserById(userId);
            model.addAttribute("user", user);
            return new ModelAndView("information");
        }else{
            String url = "redirect:/UserManagementController/toChangeEmail";
            return new ModelAndView(url);
        }
    }
    
    /**     
     * @author 胡亚星
     * @date 2018/5/5 10:17  
     * @param   
     * @return   
     *@Description:跳转到修改密码
     */
    @RequestMapping("toChangePassword")
    public ModelAndView toChangePassword(){
        return new ModelAndView("changePassword");
    }

    /**+
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/2 18:03
     * @Description:修改密码,判断原先密码是否正确
     */
    @RequestMapping("checkPassword")
    public ModelAndView checkPassword(Model model, HttpServletRequest request, Long userId) {
        String user_password = request.getParameter("user_password");
        String user_rePassword = request.getParameter("user_rePassword");
        System.out.println(user_password);
        System.out.println(user_rePassword);
        if (userService.checkPassWord(user_password, userId)) {//正确ze修改密码
            System.out.println("正确");
            model.addAttribute("mgs", "修改成功");
            userService.changePassword(user_rePassword, userId);
            return new ModelAndView("login");
        } else {//原密码错误
            model.addAttribute("userId",userId);
            model.addAttribute("mgs", "原密码错误");
            return new ModelAndView("redirect:/toChangePassword");
        }
    }


    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/4/27 20:06
     * @Description:发送验证码
     */
    @RequestMapping("sendChangePhone")
    public void sendChangePhone(HttpServletRequest request) {
        String user_mail = request.getParameter("user_mail");
        logUserService.sendEmail1(request, user_mail);//发送
    }

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/2 18:19
     * @Description:用户电话的修改
     */
    @RequestMapping("changePhone")
    public ModelAndView changePhone(Model model, HttpServletRequest request) {
        Map<String, String[]> properties = request.getParameterMap();
        String id = request.getParameter("userId");
        long userId = Long.parseLong(id);
        User user = userService.findUserById(userId);
        try {
            BeanUtils.populate(user, properties);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        String user_phone = request.getParameter("user_phone");
        user.setUser_phone(user_phone);
        userService.edit(user);
        model.addAttribute("user", user);
        return new ModelAndView("information");
    }

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/3 8:51
     * @Description:跳转到找回密码页面
     */
    @RequestMapping("toSendRetrievePassword")
    public ModelAndView toSendRetrievePassword() {
        return new ModelAndView("toSendRetrievePassword");
    }

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/3 8:43
     * @Description:找回密码发送邮箱
     */
    @RequestMapping("sendRetrievePassword")
    public void sendRetrievePassword(HttpServletRequest request, String user_mail) {
        logUserService.sendEmail2(request, user_mail);//发送
    }


    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/2 17:57
     * @Description:找回密码验证
     */
    @RequestMapping("passwordContrastCode")
    public ModelAndView passwordContrastCode(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession();
        String user_mail = "";
        String sessionMail = "";
        String usercode = "";
        String sessioncode = "";
        if (logUserService.contrastCode(request,user_mail,sessionMail,usercode,sessioncode) ){ //对比两个code是否正确
            model.addAttribute("user_mail", user_mail);
            System.out.println("222");
            return new ModelAndView("retrievePassword");
        } else {
            model.addAttribute("mag", "请检查您的验证码或邮箱是否正确");
            System.out.println("111");
            return new ModelAndView("toSendRetrievePassword");
        }
    }

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/3 8:46
     * @Description: 重置密码
     */
    @RequestMapping("retrievePassword")
    public ModelAndView retrievePassword(Model model, HttpServletRequest request, String user_mail) {
        String user_password = request.getParameter("user_password");
        userService.changePassword(user_password, user_mail);
        model.addAttribute("mgs", "修改成功");
        return new ModelAndView("login");
    }


}