package cn.blog.controller.backend;

import cn.blog.bo.BlogBo;
import cn.blog.bo.TagsAndBlog;
import cn.blog.common.Const;
import cn.blog.common.ResponseCode;
import cn.blog.common.ServerResponse;
import cn.blog.pojo.Blog;
import cn.blog.service.CacheService.CacheService;
import cn.blog.service.IBlogService;
import cn.blog.service.ITagService;
import cn.blog.vo.*;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * @Description: 后台 博客contrller
 * Created by Jann Lee on 2018/1/20  0:34.
 */
@Controller
@RequestMapping("/manage/blog")
@Slf4j
public class BlogController {

    @Autowired
    private IBlogService iBlogService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private ITagService iTagService;
    /**
     * 新增/修改博客
     * @param   blog{title,content,categoryId,
     *              [id],[createTime],[code],[author],
     *              [viewCount],[likeCount],[shareCount],
     *              [commentCount],[updateTime],[tags],[imgUri])}
     * @return
     */
    @RequestMapping(value = "saveOrUpdate.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add( Blog  blog,@RequestParam("tagIds")String tagIds) {
        log.info("接受到的参数："+blog+" "+tagIds);
        return iBlogService.saveOrUpdate(blog,tagIds);
    }

    /**
     * 高复用查询博客列表,
     * @param code       博客的类型（私有1,公开0,推荐2）
     * @param title      博客标题 模糊匹配
     * @param tagId      标签id
     * @param categoryId 分类id
     * @return ServerResponse
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(Integer code,
                                         String title,
                                         Integer tagId,
                                         Integer categoryId,
                                         Boolean isCalc,
                                         @RequestParam(value="orderBy" ,required = false)String orderBy,
                                         @RequestParam(value="pageNum",defaultValue = "1")int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {
        return  iBlogService.listByCodeTitleTagCategory(code,title,orderBy,tagId,categoryId,isCalc,pageNum,pageSize);
    }


    @RequestMapping("initBlogList.do")
    @ResponseBody
    public ServerResponse<BlogListVo> listWithCategory(Integer code,
                                                       String title,
                                                       Integer tagId,
                                                       Integer categoryId,
                                                       Boolean isCalc,
                                                       @RequestParam(value="orderBy" ,required = false)String orderBy,
                                                       @RequestParam(value="pageNum",defaultValue = "1")int pageNum,
                                                       @RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {

        ServerResponse<PageInfo> response = iBlogService.listByCodeTitleTagCategory(code,title,orderBy,tagId,categoryId,isCalc,pageNum,pageSize);
        BlogListVo blogListVo = new BlogListVo();
        if(response.isSuccess()){
            List<CategoryVo> categoryVoList = cacheService.findAllCategoryWithBlogCount();
            PageInfo pageInfo = response.getData();
            blogListVo.setCategoryVoList(categoryVoList);
            blogListVo.setPageInfo(pageInfo);
        }


        return  ServerResponse.createBySuccess(blogListVo);
    }


    /**
     * 给博客添加标签
     * @param blogId 博客id
     * @param tagId  标签id
     * @return
     */
    @RequestMapping("addTag.do")
    @ResponseBody
    public ServerResponse addTagsToBlog(Integer blogId ,Integer tagId){

        return iBlogService.addTagToBlog(blogId,tagId);
    }


    @RequestMapping("editInit.do")
    @ResponseBody
    public ServerResponse<ArticleVo> detail(Integer blogId){
        if(blogId==null){
            return ServerResponse.createByErrorCodeAndMessage(ResponseCode.NULL_ARGUMENT.getCode()
                    ,ResponseCode.NULL_ARGUMENT.getDesc());
        }
        boolean result = iBlogService.isExists(blogId);
        if(!result){
            return ServerResponse.createByErrorCodeAndMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //未使用缓存
//        List<TagVo> tagVoList = cacheService.findTagsWithCount();
//        List<CategoryVo>  categoryVoList = iCategoryService.findAllWithCount();
        //使用缓存
        List<TagVo> tagVoList = iTagService.listAllSimple();
        List<CategoryVo> categoryVoList = cacheService.findAllCategoryWithBlogCount();
        ArticleVo articleVo = new ArticleVo();


        BlogVo blogVo = iBlogService.descVo(blogId);


        articleVo.setBlogVo(blogVo);
        articleVo.setTagVoList(tagVoList);
        articleVo.setCategoryList(categoryVoList);

        return ServerResponse.createBySuccess(articleVo);
    }

    @RequestMapping(value="/delete.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse delete(Integer blogId){
        return iBlogService.deleteBlog(blogId);
    }

}
