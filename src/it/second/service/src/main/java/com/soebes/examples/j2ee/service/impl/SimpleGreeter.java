package com.soebes.examples.j2ee.service.impl;


import javax.ejb.LocalHome;
import javax.ejb.Stateless;

import com.soebes.examples.j2ee.domain.Name;
import com.soebes.examples.j2ee.sevice.api.Greeter;


@Stateless
@LocalHome(Greeter.class)
public class SimpleGreeter implements Greeter {

    @Override
    public String greet(Name name) {
        return "Hello " + name + "!";
    }

}
