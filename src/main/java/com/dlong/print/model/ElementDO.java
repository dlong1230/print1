package com.dlong.print.model;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义元素实体
 *
 * @author 蓝鲸
 * @date 2023/03/12
 */
public class ElementDO implements Serializable {

    /** 主键id */
    private Long id;

    /** 自定义元素名称 */
    private String name;

    /** 元素类型：1-文字，2-图片，3-二维码，4-条形码，5-表格 */
    private Integer type;

    private List<TabDO> tabList;

    private String selectSql;

    private Object value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<TabDO> getTabList() {
        return tabList;
    }

    public void setTabList(List<TabDO> tabList) {
        this.tabList = tabList;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
