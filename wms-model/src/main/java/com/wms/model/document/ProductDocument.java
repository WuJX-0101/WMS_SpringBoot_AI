package com.wms.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

/**
 * 商品 ES 文档
 * 索引名: wms_product
 */
@Data
@Document(indexName = "wms_product")
public class ProductDocument {

    @Id
    private Long id;

    /** 商品编码 - 精确匹配 */
    @Field(type = FieldType.Keyword)
    private String productCode;

    /** 商品名称 - IK 分词全文检索 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String productName;

    /** 条码 - 精确匹配 */
    @Field(type = FieldType.Keyword)
    private String barcode;

    /** 品牌 - 精确匹配 */
    @Field(type = FieldType.Keyword)
    private String brand;

    /** 规格 - IK 分词 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String specification;

    /** 分类名称 - 冗余字段，便于搜索展示 */
    @Field(type = FieldType.Keyword)
    private String categoryName;

    /** 售价 */
    @Field(type = FieldType.Double)
    private BigDecimal salePrice;

    /** 状态 */
    @Field(type = FieldType.Integer)
    private Integer status;

    /** 创建时间 */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private String gmtCreate;
}
