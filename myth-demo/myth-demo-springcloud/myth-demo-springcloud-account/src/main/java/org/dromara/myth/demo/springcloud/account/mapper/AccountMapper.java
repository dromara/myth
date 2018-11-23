package org.dromara.myth.demo.springcloud.account.mapper;


import org.dromara.myth.demo.springcloud.account.api.entity.AccountDO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * The interface Account mapper.
 *
 * @author xiaoyu
 */
public interface AccountMapper {

    /**
     * 扣减账户余额
     *
     * @param accountDO 实体类
     * @return rows int
     */
    @Update("update account set balance =#{balance}," +
            " update_time = #{updateTime}" +
            " where user_id =#{userId}  and  balance > 0 ")
    int update(AccountDO accountDO);


    /**
     * 根据userId获取用户账户信息
     *
     * @param userId 用户id
     * @return AccountDO account do
     */
    @Select("select * from account where user_id =#{userId}")
    AccountDO findByUserId(String userId);
}
