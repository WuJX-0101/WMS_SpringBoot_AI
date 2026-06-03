package com.wms.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 客户 ES 文档
 * 索引名: wms_customer
 */
@Data
@Document(indexName = "wms_customer")
public class CustomerDocument {

    @Id
    private Long id;

    /** 客户编码 - 精确匹配 */
    @Field(type = FieldType.Keyword)
    private String customerCode;

    /** 客户名称 - IK 分词全文检索 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String customerName;

    /** 联系人 - IK 分词 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String contactPerson;

    /** 联系电话 - 精确匹配 */
    @Field(type = FieldType.Keyword)
    private String contactPhone;

    /** 地址 - IK 分词 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String address;

    /** 客户类型 */
    @Field(type = FieldType.Integer)
    private Integer customerType;

    /** 状态 */
    @Field(type = FieldType.Integer)
    private Integer status;

    /** 创建时间 */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private String gmtCreate;
}
