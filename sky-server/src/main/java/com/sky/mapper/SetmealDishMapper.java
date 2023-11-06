package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /*
    * MyBatis 会自动将查询结果映射到 List<SetmealDish> 中。
    * 具体的映射方式是 MyBatis 会根据查询结果集的列名与 Java 对象 SetmealDish 的属性名进行匹配。
    * 如果列名与属性名匹配，MyBatis 会将结果集中的值设置到相应的属性上，然后将这个对象添加到 List 中。
    * */
    //根据id获取和套餐关联的菜品数据，放进List<SetmealDish>中存起来
    @Select("select * from setmeal_dish where setmeal_id =#{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
