## API接口文档

说明

- url是相对的, 比如登录: *user/login* ,它的完整地址是:  *http://app800.cn/safe/api/user/login*
- 没有特别说明, 相应都是json格式数据.
- 没有特别说明, 均为POST请求

------------------

### 1.用户

#### 1.1 登录1

| Request | POST | user/login |     |          |
|---------|------|-----------:|-----|----------|
|         | user |     String | *   | 用户名(手机号) |
|         | pwd  |     String | *   | 密码       |

| Response |      |        |     |                                      |      
|----------|------|-------:|-----|--------------------------------------|
|          | code | String | *   | 0:成功; 其他值:失败                         |
|          | msg  | String | *   | 错误消息                                 |
|          | data | String | ?   | code=0时,登录返回的令牌(token),后续需要登录的接口携带此值 |

#### 1.2 登录2

| Request | POST | user/login2 |     |          |
|---------|------|------------:|-----|----------|
|         | user |      String | *   | 用户名(手机号) |
|         | pwd  |      String | *   | 密码       |

| Response |       |        |     |                                |      
|----------|-------|-------:|-----|--------------------------------|
|          | code  | String | *   | 0:成功; 其他值:失败                   |
|          | msg   | String | *   | 错误消息                           |
|          | token | String | *   | code=0有效, 登录成功的令牌              |
|          | data  | Object | ?   | code=0时, 用户信息, 同 2.用户信息返回的data |

#### 1.3 用户信息

| Request | POST   | user/info |     |                               |
|---------|--------|----------:|-----|-------------------------------|
|         | token  |    String | *   | 登录时返回的令牌                      |
|         | userId |    String | ?   | 可选,指定用户的ID, 如果缺失,则返回当前登录用户的信息 |

| Response |      |             |        |              |      
|----------|------|------------:|-------:|--------------|
|          | code |      String |      * | 0:成功; 其他值:失败 |
|          | msg  |      String |      * | 错误消息         |
|          | data |      Object |      ? | code=0时有效    |
|          | -    |          id |    Int | 用户ID         |
|          | -    |    userName | String | 用户名称(姓名)     |
|          | -    |       phone | String | 手机号          |
|          | -    | portraitUrl | String | 头像URL        |
|          | -    |   projectId |    Int | 所在项目         |
|          | -    |      teamId |    Int | 所在小组         |
|          | -    |   companyId |    Int | 工作单位         |
|          | -    |  profession | String | 专业           |
|          | -    |       jobId | String | 工种           |
|          | -    |   workState |    Int | 0:在岗, 1:离岗   |

#### 1.4 注销登录

| Request | POST  | user/logout |     |          |
|---------|-------|------------:|-----|----------|
|         | token |      String | *   | 登录时返回的令牌 |

| Response |      |        |     |              |      
|----------|------|-------:|-----|--------------|
|          | code | String | *   | 0:成功; 其他值:失败 |
|          | msg  | String | *   | 错误消息         |
 