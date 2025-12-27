package com.sky.controller.user;


import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(userId);
        List<AddressBook> addressBookList = addressBookService.list(addressBook);
        return Result.success(addressBookList);

    }

    @PostMapping
    @ApiOperation("新增地址")
    public Result save(@RequestBody AddressBook addressBook){

        log.info("新增地址：{}",addressBook);
        addressBookService.save(addressBook);
        return Result.success();
    }


    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id) {

        log.info("根据id查询地址用于修改回显：{}",id);
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }


    /**
     * 根据id修改地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result update(@RequestBody AddressBook addressBook) {
        addressBookService.update(addressBook);
        return Result.success();
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.setDefault(addressBook);
        return Result.success();
    }


    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteById(Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault(){

        log.info("查询默认地址用于下单界面回显，复用list");
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(1);
        List<AddressBook> addressBooks = addressBookService.list(addressBook);
        if(addressBooks != null && addressBooks.size() == 1){
            return Result.success(addressBooks.get(0));
        }
        return Result.error("没有查询到默认地址");
    }


}
