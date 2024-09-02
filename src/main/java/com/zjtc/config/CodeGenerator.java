package com.zjtc.config;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

/**
 * @author YanFa * @author: way * @date: 2024/5/21 13:36
 */
public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://192.168.80.218:3306/ztsmartcard", "root", "MaJian880926")
                .globalConfig(
                        builder -> {
                            builder
                                    .author("way") // 设置作者
                                    .fileOverride() // 覆盖已生成文件
                                    .disableOpenDir() // 禁止打开输出目录
                                    //.outputDir("D:\\work\\code\\PosService\\src\\main\\java"); // wh
                                    .outputDir("D:\\project\\WaterConrtrolService\\src\\main\\java"); // cs
                        })
                .packageConfig(
                        builder -> {
                            builder
                                    .controller("controller")
                                    .service("service")
                                    .mapper("mapper")
                                    .parent("com") // 设置父包名
                                    .moduleName("zjtc") // 设置父包模块名
                                    .entity("entity"); // 设置entity包名
                                    //.pathInfo(
                                    //Collections.singletonMap(OutputFile.mapperXml, "D://")); //
                                    // 设置mapperXml生成路径
                        })
                .strategyConfig(
                        builder -> {
                            builder
                                    // 设置生成表名
                                    .addInclude("v_employee_data")
                                    // 开启lombok
                                    .entityBuilder()
                                    .enableLombok()
                                    // 开启属性注解@TableField&&@TableId
                                    .enableTableFieldAnnotation()
                                    // 开启restcontroller
                                    .controllerBuilder()
                                    .enableRestStyle();
                        })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
