package com.sky.controller.user;


import com.sky.annotation.AutoFill;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "c端地址接口")
@Slf4j
public class AddressBookController {


    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/list")
    @ApiOperation("查询所有地址信息")
    public Result<List<AddressBook>> list(){

        log.info("查询所有地址信息");
        List<AddressBook> addressBookList = addressBookService.list();
        return Result.success(addressBookList);

    }


}
