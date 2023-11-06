package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //向菜品表插入一条数据
        dishMapper.insert(dish);

        //获取insert生成的主键，给后续的口味用
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     */
    @Override
    @Transactional
    public void delteBatch(List<Long> ids) {
        //1.判断当前菜品是否能删除--是否起售中？
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
//            System.out.println(dish.getStatus());
            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
                //当前菜品处于起售状态，不可删除
//                System.out.println("----------------------------------------");
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2.判断当前菜品是否能删除--是否被套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //当前菜品被关联，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //3.到这一步时，可以删除菜品表中的菜品数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //4.并且删除与菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        //需要获取：1、菜品数据Dish 2、口味数据，并封装到DishVO
        //1.获取Dish数据
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> flavorById = dishFlavorMapper.getFlavorById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavorById);
        return dishVO;
    }

    @Override
    /**
     * 修改菜品
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
        //传进来的是一个DishDTO，需要修改的是dish对象，包括修改人，修改时间等
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品的基本信息
        dishMapper.update(dish);

        //删除菜品原有的所有味道
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //重新添加菜品的所有味道
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //需要做一些判断
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        //需要封装一个Dish对象供mapper查询
        //categoryId符合参数且处于起售状态下的菜品
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }
}
