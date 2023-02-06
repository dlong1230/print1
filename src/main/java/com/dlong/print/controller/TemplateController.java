package com.dlong.print.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.druid.util.StringUtils;
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
    public String getModeWithData(Long id) {

        // 获取元素列表
        String eleIds="1,2,3";

        // 获取元素详情
        Map<String,String> newMap = new HashMap<>();
        newMap.put("1","[{\"be_app_stuprof\":\"XingMing\"}]");
        newMap.put("2","[{\"be_app_stuprof\":\"XingMing\"},{\"be_app_stuprof\":\"ShenChaNR\"}]");
        newMap.put("3","[{\"be_app_stuprof\":\"ShenChaNR\"}]");

        List<List<Map<String, String>>> objList = new ArrayList<>();
        Map<String, List<Map<String, String>>> objmap = new HashMap<>();
        for (String key:newMap.keySet()){
            List<Map<String, String>> obj = (List<Map<String, String>>) JSONUtils.parse(newMap.get(key));
            objList.add(obj);
            List<Map<String, String>> tempList = objmap.get(key);
            if (tempList == null) {
                tempList = new ArrayList<>();
                objmap.putIfAbsent(key, tempList);
            }
            tempList.addAll(obj);
        }
        System.out.println(objList);
        System.out.println(objmap);

        Map<String, Set<String>> columnMap = new HashMap<>();
        for (List<Map<String, String>> list:objList) {
            for (Map<String, String> map : list) {
                String key = (String) map.keySet().toArray()[0];
                String value = (String) map.values().toArray()[0];
                Set<String> tempList = columnMap.get(key);
                if (tempList == null) {
                    tempList = new HashSet<>();
                    columnMap.putIfAbsent(key, tempList);
                }
                tempList.add(value);
            }
        }
        System.out.println(columnMap);

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        Map<String, String> columnAndValue = new HashMap<>();
        for (String table:columnMap.keySet()) {
            String sql = "select * from " + table +" where isdeleted=0 and baokaoid=1";
            List<Map<String, Object>> list_maps = jdbcTemplate.queryForList(sql);
            System.out.println(list_maps);
            map.put(table, list_maps);
            Set<String> colList = columnMap.get(table);
            for (String col:colList) {
                columnAndValue.put(table + "-" + col, (String) list_maps.get(0).get(col));
            }
        }
        System.out.println(columnAndValue);

        Map<String,String> newMap1 = new HashMap<>();
        for (String idStr : objmap.keySet()) {
            List<Map<String, String>> valueNames = objmap.get(idStr);
            List<Map<String, String>> strList = new ArrayList<>();
            for (Map<String, String> valueName : valueNames) {
                for(String valN:valueName.keySet()) {
                    String tableName = valN;
                    String cloumnName = valueName.get(valN);
                    if (valueNames.size()>1) {
                        Map<String, String> strMap = new HashMap<>();
                        String key=tableName + "-" + cloumnName;
                        strMap.put(key, columnAndValue.get(key));
                        strList.add(strMap);
                        newMap1.put(idStr, strList.toString());
                    } else {
                        newMap1.put(idStr, columnAndValue.get(tableName + "-" + cloumnName));
                    }
                }
            }
        }
        String str2 = JSONUtils.toJSONString(newMap1);
        System.out.println(str2);

//        Integer type = 1;
//        if (type != 3) {
//
//
//        }


        // 数据组装
//        Map<String, Object> mapall = new HashMap<>();
//        String name = "小王子";
//        String url = "http://deploy.yixianinfo.com/static/1e94a32b/images/svgs/logo.svg";
//        List<Map<String, Object>> list = new ArrayList<>();
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("1", name);
//        map1.put("3", url);
//        list.add(map1);
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("1", name);
//        map2.put("3", url);
//        list.add(map2);
////        String tableStr = JSONUtils.toJSONString(list);
//
//        mapall.put("1", name);
//        mapall.put("3", url);
//        mapall.put("2", list);
//        String str1 = JSONUtils.toJSONString(mapall);
//        Object obj = JSONUtils.parse(str1);
//        String sql = "select name,designStr,customEleIds from be_ticket_template where isdeleted=0 and id=" + id;
//        List<Map<String, Object>> list_maps = jdbcTemplate.queryForList(sql);
        return str2;
    }

    public static void main(String[] args) {
        Map<String,String> newMap = new HashMap<>();
        newMap.put("1","[{\"be_app_stuprof\":\"XingMing\"}]");
        newMap.put("2","[{\"be_app_stuprof\":\"XingMing\"},{\"be_app_stuprof\":\"ShenChaNR\"}]");
        newMap.put("3","[{\"be_app_stuprof\":\"ShenChaNR\"}]");

        List<List<Map<String, String>>> objList = new ArrayList<>();
        for (String key:newMap.keySet()){
            List<Map<String, String>> obj = (List<Map<String, String>>) JSONUtils.parse(newMap.get(key));
            objList.add(obj);
        }
        System.out.println(objList);
        Map<String, Set<String>> columnMap = new HashMap<>();
        for (List<Map<String, String>> list:objList) {
            for (Map<String, String> map : list) {
                String key = (String) map.keySet().toArray()[0];
                String value = (String) map.values().toArray()[0];
                Set<String> tempList = columnMap.get(key);
                if (tempList == null) {
                    tempList = new HashSet<>();
                    columnMap.putIfAbsent(key, tempList);
                }
                tempList.add(value);
            }
        }


        System.out.println(columnMap);
    }

}
