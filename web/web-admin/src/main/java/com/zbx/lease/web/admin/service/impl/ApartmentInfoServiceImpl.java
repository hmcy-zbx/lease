package com.zbx.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbx.lease.common.exception.LeaseException;
import com.zbx.lease.model.entity.*;
import com.zbx.lease.model.enums.ItemType;
import com.zbx.lease.web.admin.mapper.*;
import com.zbx.lease.web.admin.service.*;
import com.zbx.lease.common.result.ResultCodeEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbx.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.zbx.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.zbx.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.zbx.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.zbx.lease.web.admin.vo.fee.FeeValueVo;
import com.zbx.lease.web.admin.vo.graph.GraphVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private ApartmentFacilityService apartmentFacilityService;

    @Autowired
    private ApartmentLabelService apartmentLabelService;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private FeeValueMapper feeValueMapper;

    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate = apartmentSubmitVo.getId() != null;
        super.saveOrUpdate(apartmentSubmitVo);

        if (isUpdate) {
            //1.删除图片信息
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphQueryWrapper.eq(GraphInfo::getItemId, apartmentSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);

            //2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId, apartmentSubmitVo.getId());
            apartmentFacilityService.remove(apartmentFacilityQueryWrapper);

            //3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> apartmentLabelServiceQueryWrapper = new LambdaQueryWrapper<>();
            apartmentLabelServiceQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentSubmitVo.getId());
            apartmentLabelService.remove(apartmentLabelServiceQueryWrapper);

            //4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(apartmentFeeValueQueryWrapper);
        }

        //1.插入图片
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for(GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }

        //2.插入配套列表
        List<Long> facilityInfoIdList = apartmentSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIdList)){
            ArrayList<ApartmentFacility> apartmentFacilities = new ArrayList<>();
            for(Long facilityInfoId : facilityInfoIdList) {
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityInfoId);
                apartmentFacilities.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(apartmentFacilities);
        }

        //3.插入标签列表
        List<Long> labelIdList = apartmentSubmitVo.getLabelIds();
        if(!CollectionUtils.isEmpty(labelIdList)){
            ArrayList<ApartmentLabel> apartmentLabels = new ArrayList<>();
            for(Long labelId : labelIdList) {
                ApartmentLabel apartmentLabel = new ApartmentLabel();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(labelId);
                apartmentLabels.add(apartmentLabel);
            }
            apartmentLabelService.saveBatch(apartmentLabels);
        }

        //4.插入杂费列表
        List<Long> feeValueIdList = apartmentSubmitVo.getFeeValueIds();
        if(!CollectionUtils.isEmpty(feeValueIdList)){
            ArrayList<ApartmentFeeValue> apartmentFeeValues = new ArrayList<>();
            for(Long feeValueId : feeValueIdList) {
                ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValue.setFeeValueId(feeValueId);
                apartmentFeeValues.add(apartmentFeeValue);
            }
            apartmentFeeValueService.saveBatch(apartmentFeeValues);
        }
    }

    @Override
    public IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageItem(page,queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if(apartmentInfo == null){
            return null;
        }
        //1. 获取图片列表
        List<GraphVo> graphVoList =  graphInfoMapper.selectListByItemAndId(ItemType.APARTMENT,id);

        //2.获取标签信息
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);

        //3.查询配套关系
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(id);

        //4.杂费信息列表
        List<FeeValueVo> feeValueVoList =  feeValueMapper.selectListByApartmentId(id);

        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);

        return apartmentDetailVo;

    }

    @Override
    public void removeApartmentById(Long id) {

        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper.eq(RoomInfo::getApartmentId, id);
        Long count = roomInfoMapper.selectCount(roomInfoQueryWrapper);
        if(count > 0){
            //终止删除，返回提示信息
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE.getCode(), ResultCodeEnum.ADMIN_APARTMENT_DELETE.getMessage());
        }
        super.removeById(id);
        //1.删除图片信息
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapper);

        //2.删除配套列表
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId, id);
        apartmentFacilityService.remove(apartmentFacilityQueryWrapper);

        //3.删除标签列表
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelServiceQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelServiceQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        apartmentLabelService.remove(apartmentLabelServiceQueryWrapper);

        //4.删除杂费列表
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        apartmentFeeValueService.remove(apartmentFeeValueQueryWrapper);
    }
}




