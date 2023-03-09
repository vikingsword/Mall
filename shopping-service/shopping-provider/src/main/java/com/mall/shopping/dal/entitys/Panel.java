package com.mall.shopping.dal.entitys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "tb_panel")
@AllArgsConstructor
@NoArgsConstructor
public class Panel implements Serializable {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;
    private String name;
    private Integer type;
    private Integer sortOrder;
    private Integer position;
    private Integer limitNum;
    private Integer status;
    private String remark;
    private Date created;
    private Date updated;
    private List<PanelContentItem> panelContentItems;

    @Transient
    private Long productId;

    private static final long serialVersionUID = 1L;

}