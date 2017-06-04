package com.controller;

import com.bo.Article;
import com.common.ICacheService;
import com.common.SystemConst;
import com.common.util.BeanConvertor;
import com.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by Lincg on 2017/5/28.
 */
@Controller
public class AllSiteController {

    @Autowired
    private ArticleService articleService;

    @Resource
    private ICacheService cacheService;

    @RequestMapping(value = "/allSite", method = RequestMethod.GET)
    public ModelAndView getAllSite(HttpSession session, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) session.getAttribute("username");
        String lastDateTime = request.getParameter("lastDateTime");
        String lessDateTime = request.getParameter("lessDateTime");
        String pageCount = request.getParameter("pageCount");
        String tag = request.getParameter("tag");
        List<Article> articles;
        if (tag == null || tag.isEmpty()) {
            articles = articleService.getArticleList(lastDateTime, lessDateTime, Byte.valueOf("1"));
        } else {
            articles = articleService.getArticleTagList(tag, lastDateTime, lessDateTime, Byte.valueOf("1"));
        }
        long count = 0;
        String key;
        for (Article article : articles){
            count = 0;
            key = "article:ReadCount:" + article.getId();
            if (cacheService.exists(key)) {
                count = (long)cacheService.getLong(key);
            }
            article.setReadCounts((int)count);
        }
        modelAndView.addObject("tag", tag);
        if (pageCount == null || pageCount.isEmpty()) {
            modelAndView.addObject("pageCount","1");
            modelAndView.addObject("lessDateTime", null);
            if (articles != null && !articles.isEmpty() && (articles.size() > SystemConst.PAGESIZE)) {
                articles.remove(articles.size() - 1);
                modelAndView.addObject("lastDateTime", articles.get(articles.size() - 1).getCreateTime());
            } else {
                modelAndView.addObject("lastDateTime", null);
            }
        } else {
            if (lastDateTime != null && !lastDateTime.isEmpty()) {
                modelAndView.addObject("pageCount", Integer.parseInt(pageCount) + 1);
                modelAndView.addObject("lessDateTime", lastDateTime);
                if (articles != null && !articles.isEmpty() && (articles.size() > SystemConst.PAGESIZE)) {
                    articles.remove(articles.size() - 1);
                    modelAndView.addObject("lastDateTime", articles.get(articles.size() - 1).getCreateTime());
                } else {
                    modelAndView.addObject("lastDateTime", null);
                }
            } else if (lessDateTime != null && !lessDateTime.isEmpty()){
                modelAndView.addObject("pageCount", Integer.parseInt(pageCount) - 1);
                modelAndView.addObject("lastDateTime", lessDateTime);
                if (articles != null && !articles.isEmpty() && (articles.size() > SystemConst.PAGESIZE)) {
                    articles.remove(articles.size() - 1);
                    modelAndView.addObject("lessDateTime", articles.get(articles.size() - 1).getCreateTime());
                } else {
                    modelAndView.addObject("lessDateTime", null);
                }
            }
        }

        //查找标签
        List<String> tags = articleService.getTags(Byte.valueOf("1"));
        modelAndView.addObject("tags", tags);
        modelAndView.addObject("username", username);

        modelAndView.addObject("articles",
                BeanConvertor.convert2ArticleVos(articles, Byte.valueOf("0")));
        modelAndView.setViewName("/allSite");
        return modelAndView;
    }

}
