package org.wlgzs.xf_mall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wlgzs.xf_mall.base.BaseController;
import org.wlgzs.xf_mall.entity.ShippingAddress;
import org.wlgzs.xf_mall.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author:胡亚星
 * @createTime 2018-05-05 8:16
 * @description:有关收货地址的操作
 **/
@RequestMapping("UserAddressController")
@Controller
public class ShippingAddressController extends BaseController {

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/5 8:18
     * @Description:跳转到收货地址页面
     */
    @RequestMapping("shippingAddress")
    public String toShippingAddress(HttpServletRequest request, Model model, String user_name) {
        if(user_name == null){
            HttpSession session = request.getSession(true);
            User user = (User) session.getAttribute("user");
            User userTwo = userService.findUserById(user.getUserId());
            model.addAttribute("user", userTwo);
            user_name = user.getUser_name();
        }
        List<ShippingAddress> shippingAddresslists = shippingAddressService.getShippingAddressList(user_name);
        model.addAttribute("shippingAddresslists", shippingAddresslists);
        return "shippingAddress";
    }

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/5 8:30
     * @Description:新增收货地址
     */
    @RequestMapping("addShippingAddress")
    public String addShippingAddress(ShippingAddress shippingAddress,HttpServletRequest request, String user_name) {
        if(user_name == null){
            HttpSession session = request.getSession(true);
            user_name = (String) session.getAttribute("name");
        }
        //是否默认
        int address_is_default = shippingAddress.getAddress_is_default();
        //查询该用户数据是否有默认的
        if(shippingAddressService.findState(user_name)){//有
            //1变为0
            if(address_is_default == 1)shippingAddressService.modifyState(0,1);
        }else{
            if(address_is_default == 0)shippingAddress.setAddress_is_default(1);
        }
        shippingAddress.setUser_name(user_name);
        shippingAddressService.addShippingAddress(shippingAddress);
        return "redirect:shippingAddress";
    }

    /**
     * @param
     * @return
     * @author 胡亚星
     * @date 2018/5/5 8:36
     * @Description:跳到修改页面
     */
    @RequestMapping("toChangeShippingAddress")
    public String toChangeShippingAddress(Model model, long addressId) {
        ShippingAddress shippingAddress = shippingAddressService.findById(addressId);
        String address_shipping = shippingAddress.getAddress_shipping();
        String [] address= address_shipping.split(" ");
        model.addAttribute("province",address[0]);
        model.addAttribute("city",address[1]);
        model.addAttribute("area",address[2]);
        model.addAttribute("details",address[3]);
        model.addAttribute("shippingAddress", shippingAddress);
        return "ChangeShippingAddress";
    }

    /**     
     * @author 胡亚星
     * @date 2018/5/5 8:44  
     * @param   
     * @return   
     *@Description:修改
     */
    @RequestMapping("shippingAddresslists")
    public String shippingAddresslists(ShippingAddress shippingAddress){
        //是否默认
        int address_isDefault = shippingAddress.getAddress_is_default();
        //1变为0
        if(address_isDefault == 1)shippingAddressService.modifyState(0,1);
        shippingAddressService.ModifyAddress(shippingAddress);
        return "redirect:shippingAddress";
    }

    /**     
     * @author 胡亚星
     * @date 2018/5/5 8:49  
     * @param   
     * @return   
     *@Description:根据id删除
     */  
    @RequestMapping("deleteShippingAddress")
    public String deleteShippingAddress(long addressId){
        shippingAddressService.deleceAddress(addressId);
        return "redirect:shippingAddress";
    }

    /**
     * @author 胡亚星
     * @date 2018/5/26 9:03
     * @param
     * @return
     *@Description:将收货地址设为默认
     */
    @RequestMapping("setDefault")
    public String setDefault(long addressId,String user_name){
        shippingAddressService.setDefault(addressId,user_name);
        return "redirect:shippingAddress";
    }
}
