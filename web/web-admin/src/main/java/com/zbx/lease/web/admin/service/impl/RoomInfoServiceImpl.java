package com.zbx.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbx.lease.common.result.Result;
import com.zbx.lease.model.entity.*;
import com.zbx.lease.model.enums.ItemType;
import com.zbx.lease.web.admin.mapper.*;
import com.zbx.lease.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbx.lease.web.admin.vo.attr.AttrValueVo;
import com.zbx.lease.web.admin.vo.graph.GraphVo;
import com.zbx.lease.web.admin.vo.room.RoomDetailVo;
import com.zbx.lease.web.admin.vo.room.RoomItemVo;
import com.zbx.lease.web.admin.vo.room.RoomQueryVo;
import com.zbx.lease.web.admin.vo.room.RoomSubmitVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private RoomAttrValueService roomAttrValueService;

    @Autowired
    private RoomFacilityService roomFacilityService;

    @Autowired
    private RoomLabelService roomLabelService;

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private RoomLabelMapper roomLabelMapper;

    @Autowired
    private RoomPaymentTypeMapper roomPaymentTypeMapper;

    @Autowired
    private RoomLeaseTermMapper roomLeaseTermMapper;

    @Autowired
    private RoomAttrValueMapper roomAttrValueMapper;

    @Autowired
    private RoomFacilityMapper roomFacilityMapper;

    @Override
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);
        if (isUpdate) {
            //1.删除图片
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphQueryWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);

            //2.删除属性
            LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
            roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(roomAttrValueQueryWrapper);

            //3.删除配套
            LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
            roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(roomFacilityQueryWrapper);

            //4.删除标签
            LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(roomLabelQueryWrapper);

            //5.删除支付
            LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(roomPaymentTypeQueryWrapper);

            //6.删除租期
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(roomLeaseTermQueryWrapper);
        }

        //1.插入图片
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for(GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
        //2.插入属性
        List<Long> attrValueList = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueList)){
            ArrayList<RoomAttrValue> roomAttrValueList = new ArrayList<>();
            for(Long attrValueId : attrValueList) {
                RoomAttrValue roomAttrValue = new RoomAttrValue();
                roomAttrValue.setAttrValueId(attrValueId);
                roomAttrValue.setRoomId(roomSubmitVo.getId());
                roomAttrValueList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueList);
        }

        //3.插入配套
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIds)){
            ArrayList<RoomFacility> roomFacilityList = new ArrayList<>();
            for(Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility = new RoomFacility();
                roomFacility.setRoomId(roomSubmitVo.getId());
                roomFacility.setFacilityId(facilityInfoId);
                roomFacilityList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityList);
        }


        //4.插入标签
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(labelInfoIds)){
            ArrayList<RoomLabel> roomLabelList = new ArrayList<>();
            for(Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel = new RoomLabel();
                roomLabel.setRoomId(roomSubmitVo.getId());
                roomLabel.setLabelId(labelInfoId);
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }

        //5.插入支付
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if(!CollectionUtils.isEmpty(paymentTypeIds)){
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for(Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType = new RoomPaymentType();
                roomPaymentType.setRoomId(roomSubmitVo.getId());
                roomPaymentType.setPaymentTypeId(paymentTypeId);
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }

        //6.插入租期
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if(!CollectionUtils.isEmpty(leaseTermIds)){
            ArrayList<RoomLeaseTerm> roomLeaseTermList = new ArrayList<>();
            for(Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = new RoomLeaseTerm();
                roomLeaseTerm.setRoomId(roomSubmitVo.getId());
                roomLeaseTerm.setLeaseTermId(leaseTermId);
                roomLeaseTermList.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTermList);
        }

    }

    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(page, queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        //1.获取信息
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if(roomInfo == null){
            return null;
        }
        //1.获取公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());

        //2.获取图片
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemAndId(ItemType.ROOM, id);

        //3.获取属性
        List<AttrValueVo> attrValueVoList = roomAttrValueMapper.selectByRoomId(id);

        //4.获取标签
        List<LabelInfo> labelInfoList = roomLabelMapper.selectByRoomId(id);

        //获取配套
        List<FacilityInfo> facilityInfoList = roomFacilityMapper.selectByRoomId(id);

        //5.获取支付
        List<PaymentType> paymentTypeList =  roomPaymentTypeMapper.selectByRoomId(id);

        //6.获取租期
        List<LeaseTerm> leaseTermList = roomLeaseTermMapper.selectByRoomId(id);

        RoomDetailVo adminRoomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, adminRoomDetailVo);

        adminRoomDetailVo.setApartmentInfo(apartmentInfo);
        adminRoomDetailVo.setGraphVoList(graphVoList);
        adminRoomDetailVo.setAttrValueVoList(attrValueVoList);
        adminRoomDetailVo.setFacilityInfoList(facilityInfoList);
        adminRoomDetailVo.setLabelInfoList(labelInfoList);
        adminRoomDetailVo.setPaymentTypeList(paymentTypeList);
        adminRoomDetailVo.setLeaseTermList(leaseTermList);

        return adminRoomDetailVo;
    }

    @Override
    public void removeRoomById(Long id) {
        //1.删除RoomInfo
        super.removeById(id);
        //1.删除图片
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapper);

        //2.删除属性
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(roomAttrValueQueryWrapper);

        //3.删除配套
        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(roomFacilityQueryWrapper);

        //4.删除标签
        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(roomLabelQueryWrapper);

        //5.删除支付
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(roomPaymentTypeQueryWrapper);

        //6.删除租期
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(roomLeaseTermQueryWrapper);

    }
}




