package com.dlong.print.controller;

import com.dlong.print.model.ElementDO;
import com.dlong.print.model.TabDO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
        String sql = "select id field,name text from be_ticket_element where isdeleted=0 ";
        if (type != null && type > 0) {
            sql = sql + "and type=" + type;
        }
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
        return "/custom.html";
    }

    // http://localhost/template/print/addTemplate.htm?name=模板2
    @ResponseBody
    @RequestMapping("/addTemplate.htm")
    public int addTemplate(String name) {
        String sql = "INSERT INTO be_ticket_template (`id`, `name`, `schoolId`, `designStr`, `customEleIds`, `enable`, `isDeleted`, `createdBy`, `createdOn`, `modifiedBy`, `modifiedOn`, `createDate`) VALUES (null, '" + name + "', 0, NULL, NULL, 1, 0, 1, '2023-02-03 11:44:12', 1, '2023-02-03 11:44:12', 123);";
        int row = jdbcTemplate.update(sql);
        return row;
    }

    // http://localhost/template/print/updateTemplate.htm?designStr=模板2&customEleIds=1,2,3&id=1
    @ResponseBody
    @RequestMapping("/updateTemplate.htm")
    public int updateTemplate(String designStr, String customEleIds, Long id) {
        String sql = "update be_ticket_template set designStr='"+ designStr +"', customEleIds= '"+ customEleIds +"'where id=" + id;
        int row = jdbcTemplate.update(sql);
        return row;
    }

    // http://localhost/template/print/getTemplateById.htm?id=2
    @ResponseBody
    @RequestMapping("/getTemplateById.htm")
    public List<Map<String, Object>> getTemplateById(Long id) {
        String sql = "select name,designStr,customEleIds from be_ticket_template where isdeleted=0 and id=" + id;
        List<Map<String, Object>> list_maps = jdbcTemplate.queryForList(sql);
        return list_maps;
    }

    // http://localhost/template/print/getModeWithData.htm?id=2
    @ResponseBody
    @RequestMapping("/getModeWithData.htm")
    public Map<Long, Object> getModeWithData(Long id) {
        List<ElementDO> elementDOList = buildEleList();

        Set<String> sqlSet = new HashSet<>();
        for (ElementDO ele : elementDOList) {
            String sql = ele.getSelectSql();
            if (sql == null || sql == "") {
                continue;
            }
            if (ele.getType() != 5) {
                sql = sql + " limit 1";
                ele.setSelectSql(sql);
            }
            sqlSet.add(sql);
        }
        // 执行对应的sql
        Map<String, List<Map<String, Object>>> sqlMap = new HashMap<>();
        for (String sql : sqlSet) {
            List<Map<String, Object>> list_maps = jdbcTemplate.queryForList(sql);
            sqlMap.put(sql, list_maps);
        }

        Map<Long, Object> ap = new HashMap<>();
        // 遍历自定义元素，放入value
        for (ElementDO elementDO : elementDOList) {
            List<Map<String, Object>> sqlMapValueList = sqlMap.get(elementDO.getSelectSql());
            if (elementDO.getType() != 5) {
                StringBuilder eleValue = new StringBuilder();
                for (TabDO tab : elementDO.getTabList()) {
                    if (tab.getTableName() != null) {
                        eleValue.append(sqlMapValueList.get(0).get(tab.getFieldName()));
                    } else {
                        eleValue.append(tab.getFieldName());
                    }
                }
                elementDO.setValue(eleValue);
            } else {
                List<Object> listValue = new ArrayList<>();
                Map<String, String> listValueMap;
                for (Map<String, Object> valueMap : sqlMapValueList) {
                    listValueMap = new HashMap<>();
                    for (TabDO tab : elementDO.getTabList()) {
                        listValueMap.put(tab.getFieldName(), (String) valueMap.get(tab.getFieldName()));
                    }
                    listValue.add(listValueMap);
                }
                elementDO.setValue(listValue);
            }
            ap.put(elementDO.getId(), elementDO.getValue());
        }
        System.out.println(ap);
        return ap;
    }

    private List<ElementDO> buildEleList() {
        List<ElementDO> elementDOList = new ArrayList<>();
        ElementDO elementDO1 = new ElementDO();
        elementDO1.setId(1L);
        elementDO1.setName("姓名");
        elementDO1.setType(1);
        TabDO tabDO1 = new TabDO();
        tabDO1.setTableName("be_app_stuprof");
        tabDO1.setFieldName("XingMing");
        elementDO1.setTabList(Collections.singletonList(tabDO1));
        elementDO1.setSelectSql("select * from be_app_stuprof where YongHuID=1078403");
        System.out.println(elementDO1);
        elementDOList.add(elementDO1);

        ElementDO elementDO2 = new ElementDO();
        elementDO2.setId(6L);
        elementDO2.setName("性别");
        elementDO2.setType(1);
        TabDO tabDO2 = new TabDO();
        tabDO2.setTableName("be_app_stuprof");
        tabDO2.setFieldName("XingBie");
        elementDO2.setTabList(Collections.singletonList(tabDO2));
        elementDO2.setSelectSql("select * from be_app_stuprof where YongHuID=1078403");
        System.out.println(elementDO2);
        elementDOList.add(elementDO2);

        ElementDO elementDO3 = new ElementDO();
        elementDO3.setId(7L);
        elementDO3.setName("组合（姓名+性别）");
        elementDO3.setType(1);
        List<TabDO> tablist3 = new ArrayList<>();
        tablist3.add(tabDO1);
        TabDO tabDO3 = new TabDO();
        tabDO3.setFieldName("（");
        tablist3.add(tabDO3);
        tablist3.add(tabDO2);
        elementDO3.setTabList(tablist3);
        elementDO3.setSelectSql("select * from be_app_stuprof where YongHuID=1078403");
        System.out.println(elementDO3);
        elementDOList.add(elementDO3);

        ElementDO elementDO4 = new ElementDO();
        elementDO4.setId(2L);
        elementDO4.setName("表格（id）");
        elementDO4.setType(5);
        List<TabDO> tablist4 = new ArrayList<>();
        TabDO tabDO4 = new TabDO();
        tabDO4.setTableName("be_app_stuprof");
        tabDO4.setFieldName("XueXiaoMC");
        tablist4.add(tabDO4);
        TabDO tabDO5 = new TabDO();
        tabDO5.setTableName("be_app_stuprof");
        tabDO5.setFieldName("KaoShiMC");
        tablist4.add(tabDO5);
        TabDO tabDO6 = new TabDO();
        tabDO6.setTableName("be_app_stuprof");
        tabDO6.setFieldName("KaoDianMC");
        tablist4.add(tabDO6);
        TabDO tabDO7 = new TabDO();
        tabDO7.setTableName("be_app_stuprof");
        tabDO7.setFieldName("ZhuanYeMC");
        tablist4.add(tabDO7);
//        TabDO tabDO8 = new TabDO();
//        tabDO8.setTableName("be_app_stuprof");
//        tabDO8.setFieldName("ZhuanYeMC");
//        tablist4.add(tabDO8);
        elementDO4.setTabList(tablist4);
        elementDO4.setSelectSql("select * from be_app_stuprof where YongHuID=1078403");
        elementDOList.add(elementDO4);
        System.out.println(elementDO4);
        return elementDOList;
    }

}
