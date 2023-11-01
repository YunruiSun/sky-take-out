package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品及其对应口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
