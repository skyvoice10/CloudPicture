package com.example.yupicturebackend.manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * 图片分表算法
 */
public class PictureShardingAlgorithm  implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String>  availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
            Long spaceId= preciseShardingValue.getValue();
            String logicTableName= preciseShardingValue.getLogicTableName();
            //  spaceId为null表示查询所有图片
            if(spaceId==null){
                return logicTableName;
            }
            // 根据spaceId动态生成表名
            String realTableName="picture_"+spaceId;
            // availableTargetNames 始终为 picture，无法返回真实的分表
            if(availableTargetNames.contains(realTableName)){
                return realTableName;
            }
            else{
                return logicTableName;
            }
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return new ArrayList<>();
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {

    }
}
