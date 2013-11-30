package org.mmah.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 * Date: 11/16/13
 * Time: 8:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/patient")
public class PatientController {
    @RequestMapping("/new")
    public String createPatient() {
        return "patient created";
    }
}
