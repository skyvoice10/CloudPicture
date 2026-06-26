package com.example.yupicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.manager.sharding.DynamicShardingManager;
import com.example.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.example.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.example.yupicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.example.yupicturebackend.model.entity.Picture;
import com.example.yupicturebackend.model.entity.Space;
import com.example.yupicturebackend.model.entity.SpaceUser;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.SpaceLevelEnum;
import com.example.yupicturebackend.model.enums.SpaceRoleEnum;
import com.example.yupicturebackend.model.enums.SpaceTypeEnum;
import com.example.yupicturebackend.model.vo.PictureVo;
import com.example.yupicturebackend.model.vo.SpaceVo;
import com.example.yupicturebackend.model.vo.UserVo;
import com.example.yupicturebackend.model.vo.space.analyze.SpaceUsageAnalyzeResponse;
import com.example.yupicturebackend.service.SpaceService;
import com.example.yupicturebackend.mapper.SpaceMapper;
import com.example.yupicturebackend.service.SpaceUserService;
import com.example.yupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-02-02 12:02:48
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    UserService userService;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    SpaceUserService spaceUserService;

//    @Resource
//    @Lazy
//    DynamicShardingManager dynamicShardingManager;

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        if (spaceQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误");
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();

        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);

        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;


    }

    @Override
    public SpaceVo getSpaceVo(Space space, HttpServletRequest request) {
        //  对象转变成封装类
        SpaceVo spaceVo = SpaceVo.objToVo(space);
        //  关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            spaceVo.setUserVo(userVo);
        }
        return spaceVo;
    }

    @Override
    public void validateSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        //  从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        //  创建时校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名不能为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            ThrowUtils.throwIf(spaceType == null, ErrorCode.PARAMS_ERROR, "空间类别不能为空");
        }

        //  修改数据时空间名称进行校验
        ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName) && spaceName.length() > 30,
                ErrorCode.PARAMS_ERROR, "空间名过长");
        //  修改数据时空间级别进行校验
        ThrowUtils.throwIf(spaceLevel != null && spaceLevelEnum == null,
                ErrorCode.PARAMS_ERROR, "空间级别不存在");
        //  修改数据时空间类别进行校验
        ThrowUtils.throwIf(spaceType != null && spaceTypeEnum == null,
                ErrorCode.PARAMS_ERROR, "空间类别不存在");
    }

    @Override
    public Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaces = spacePage.getRecords();
        Page<SpaceVo> spaceVoPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaces)) {
            return spaceVoPage;
        }
        //  对象列表->封装对象列表
        List<SpaceVo> spaceVoList = spaces.stream().map(SpaceVo::objToVo).collect(Collectors.toList());
        //  1.关联查询用户信息
        Set<Long> userIdSet = spaces.stream().map(Space::getUserId).collect(Collectors.toSet());
        //  1->[user1],2->[user2](列表中只有一条数据)
        //  分页查询可能会查某个用户的多个空间，为了减少对用户查询的次数使用set记录用户id，同一用户的多个空间只用查一遍用户信息
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        //  2.填充信息
        spaceVoList.forEach(spaceVo -> {
            Long userId = spaceVo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVo.setUserVo(userService.getUserVo(user));
        });
        spaceVoPage.setRecords(spaceVoList);
        return spaceVoPage;


    }

    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            //  管理员可以指定更大的maxCount和maxSize
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //  1.填充默认参数
        //  转换实体类和DTO
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        //  指定默认空间级别
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //  指定默认空间类型
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        //  填充数据
        fillSpaceBySpaceLevel(space);
        //  2.校验参数
        this.validateSpace(space, true);
        //  3.校验权限，非管理员只能创建普通级别的空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (!space.getSpaceLevel().equals(SpaceLevelEnum.COMMON.getValue()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        //  4.控制同一用户只能创建一个私有的空间
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            //  保证放锁的同时数据已提交
            Long newSpaceId = transactionTemplate.execute(status -> {
                //  判断是否已有空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType,space.getSpaceType())
                        .exists();
                //  如果已有空间则不能创建
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能有一个空间");
                //  创建
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存空间到数据库失败");
                //  创建成功后，如果是团队空间，关联新增团队成员记录
                if(space.getSpaceType()==SpaceTypeEnum.TEAM.getValue()){
                    SpaceUser spaceUser=new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(space.getUserId());
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    result= spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"创建团队成员记录失败");
                }
//                //  创建分表
//                dynamicShardingManager.createSpacePictureTable(space);

                //  返回新写入数据的id
                return space.getId();
            });
            return newSpaceId;
        }

    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足");
        }
    }


}




