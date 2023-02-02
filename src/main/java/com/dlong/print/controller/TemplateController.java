package com.dlong.print.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板控制器
 *
 * @author 蓝鲸
 * @date 2023/02/02
 */
@Controller
@RequestMapping("/template/print/*")
public class TemplateController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    // http://localhost/template/print/tableList.htm
    @ResponseBody
    @RequestMapping("/tableList.htm")
    public List<Map<String, Object>> queryTableList() {
        String sql = "show tables";
        List<Map<String, Object>> list_maps = jdbcTemplate.queryForList(sql);
        return list_maps;
    }

    // http://localhost/template/print/columnList.htm?tableName=be_app_stuprof
    @ResponseBody
    @RequestMapping("/columnList.htm")
    public List<Map> queryColumnList(String tableName) {
        String sql = "show full fields from " + tableName;
        List<Map> list = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                Map map = new HashMap();
                map.put("field",resultSet.getString("Field"));
                map.put("doc",resultSet.getString("Comment"));
                return map;
            }
        });
        return list;
    }

    // http://localhost/template/print/insertEle.htm?eleName=%E5%A7%93%E5%90%8D&eleType=1&needEncrypt=2&elementStructure=[{%22be_app_stuprof%22,%22BaoKaoID%22}]
    @ResponseBody
    @RequestMapping("/insertEle.htm")
    public int queryColumnList(String eleName, String eleType, Integer needEncrypt, String elementStructure) {
        String sql = "INSERT INTO be_ticket_element (`id`, `name`, `type`, `needEncrypt`, `elementStructure`, `remark`, `isDeleted`, `createdBy`, `createdOn`, `modifiedBy`, `modifiedOn`, `createDate`) VALUES (null, '" + eleName + "'," + eleType + "," + needEncrypt + ",'" + elementStructure + "', null, 0, 1, NOW(), 1, NOW(), 123)";
        int row = jdbcTemplate.update(sql);
        return row;
    }

    // http://localhost/template/print/queryEleList.htm?type=1
    @ResponseBody
    @RequestMapping("/queryEleList.htm")
    public List<Map<String, Object>> queryEleList(Integer type) {
        String sql = "select id,name from be_ticket_element where isdeleted=0 and type=" + type;
        List<Map<String, Object>> list_maps = jdbcTemplate.queryForList(sql);
        return list_maps;
    }

    // http://localhost/template/print/queryByIdList.htm?idList=1
    @ResponseBody
    @RequestMapping("/queryByIdList.htm")
    public List<Map<String, Object>> queryByIdList(String idList) {
        String sql = "select * from be_ticket_element where isdeleted=0 and id in (" + idList + ")";
        List<Map<String, Object>> list_maps = jdbcTemplate.queryForList(sql);
        return list_maps;
    }
    @RequestMapping("/print.htm")
    public String print() {
        return "custom.html";
    }

}
