# 讯方项目约定与模式

## 目录结构
```
xunfang-modules/xunfang-{module}/
  src/main/java/com/xunfang/{module}/
    domain/        → 实体类，extends BaseEntity
    controller/    → @RestController + @RequestMapping + extends BaseController
    service/       → I*Service 接口
    service/impl/  → @Service 实现类
    util/          → 工具类（DMEUtil/RequestUtil等）
  src/main/resources/
    bootstrap.yml  → server.port + Nacos + Redis 配置
```

## Controller 模式
```java
@RestController
@RequestMapping("/manufacture/{resource}")
public class XxxController extends BaseController {
    @Resource
    private IXxxService xxxService;

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) { return success(service.selectById(id)); }

    @GetMapping("/list")
    public TableDataInfo list(Xxx entity, HttpServletRequest request) { startPage(); return service.selectList(entity, request); }

    @PostMapping
    public AjaxResult add(@RequestBody Xxx entity) { return service.insert(entity); }

    @PutMapping
    public AjaxResult edit(@RequestBody Xxx entity) { return service.update(entity); }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) { return service.deleteByIds(ids); }

    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) { return service.deleteById(id); }
}
```

## Service 模式（无数据库，直接调 iDME）
- 注入 `@Autowired DMEUtil dmeUtil`
- URL = `DMEUtil.projectUrl + DMEUtil.executeApiUrl + "实体名/操作"`
- 分页：从 request 取 pageNum/pageSize，兜底 1/10
- filter 必须 `params.put("filter", filter)`
- 返回结果判断：`result != null && "SUCCESS".equals(result.toString())`

## 命名约定
- 控制器：`XxxController` → `@RequestMapping("/manufacture/{resource}")`
- Service 接口：`IXxxService`
- Service 实现：`XxxServiceImpl`
- Domain：`Xxx` extends `BaseEntity`
- 包名：`com.xunfang.manufacture`

## 统一返回
- 成功：`AjaxResult.success()` 或 `AjaxResult.success(data)`
- 失败：`AjaxResult.error()` 或 `AjaxResult.error("msg")`
- 分页：`TableDataInfo`（code/rows/total/msg）
- BaseController 提供 `success()`/`error()`/`startPage()` 快捷方法

## bootstrap.yml 必备配置
```yaml
server:
  port: 9205
spring:
  servlet:
    multipart:
      max-file-size: 50MB      # 必须 > 默认 1MB
      max-request-size: 50MB
  # Gateway 用 spring.codec.max-in-memory-size 代替 servlet.multipart

## iDME API 路径（以 XfPart17 为例）
| 操作 | 路径 |
|---|---|
| 分页查询 | `projectUrl + /dynamic/api/XfPart17/find/{pageSize}/{pageNum}` (POST) |
| 创建 | `projectUrl + /dynamic/api/XfPart17/create` (POST) |
| 更新 | `projectUrl + /dynamic/api/XfPart17/update` (POST) |
| 详情 | `projectUrl + /dynamic/api/XfPart17/get` (POST) |
| 删除 | `projectUrl + /dynamic/api/XfPart17/delete` (POST) |
| 批量删除 | `projectUrl + /dynamic/api/XfPart17/batchDelete` (POST) |
| 检出 | `projectUrl + /dynamic/api/XfPart17/checkout` (POST) |
| 检入 | `projectUrl + /dynamic/api/XfPart17/checkin` (POST) |
| 上传 | `basicUrl + /services/rdm/basic/api/upload/uploadFile?...` (POST multipart) |
| 下载 | `basicUrl + /services/rdm/basic/api/v2/file/downloadFile?...` (GET) |

## iDME 实体附加常量（XfPart 参考）
```java
public static final String modelNumber = "XfPart17";   // 实体名
public static final String modelName = "XfPart17";     // 实体名
public static final String modelCode = "DM08699761";   // 实体编码（上传/下载 API 使用）
```
DMEUtil 附加: `appIdShort = "9fc851035ebc468fb3e71455d6664f24"`（下载/上传 API 使用）
