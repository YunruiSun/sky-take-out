package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询对应套餐
     */
    List<Long> getSetmealIdByDishIds(List<Long> dishIds);

    /**
     * 批量保存套餐和菜品的关联关系
     *
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 删除和套餐关联的菜品
     *
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id =#{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
