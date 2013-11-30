package org.mmah.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Date: 11/16/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/account")
public class AccountController {
    @RequestMapping("/new")
    @ResponseBody
    public String createAccount() { // HttpServletRequest request
        return "new account created!";
    }

    @RequestMapping("/{accountId:\\d+}/")
    @ResponseBody
    public String getAccount(@PathVariable long accountId) {
        return "this is account " + accountId;
    }
}
