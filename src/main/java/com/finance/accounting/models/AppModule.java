package com.finance.accounting.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "app_modules",
        uniqueConstraints = @UniqueConstraint(columnNames = "module_code"))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppModule extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_code", nullable = false, length = 128)
    private String moduleCode;

    @Column(name = "menu_path", nullable = false, length = 512)
    private String menuPath;

    @Column(name = "form_name", nullable = false)
    private String formName;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
