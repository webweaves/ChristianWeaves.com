package com.christianweaves.controllers;

import com.christianweaves.entities.Article;
import com.christianweaves.entities.ArticleArchive;
import com.christianweaves.entities.ArticleDao;
import com.christianweaves.entities.GenericDao;
import com.christianweaves.entities.PageContents;
import com.christianweaves.entities.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@Named
@RequestScoped
public class ArticleController {

	@Inject
	private ArticleDao articleDao;
	
	@Inject
	private GenericDao genericDao;
	
	@Inject 
	private ApplicationController applicationController;
	
	@Inject TagController tagController;
	
	@Resource 
	private UserTransaction userTransaction; 

	private List<String> formTags;
	
	@SuppressWarnings("unused")
	private Article newArticle = new Article();
	
	private List<String> filters = Arrays.asList(new String[] {"^<p>&nbsp;</p>", "\\r\\n\\r\\n"});
	/**
	 * return the article based on id
	 * 
	 * @return
	 */
	public Article getArticleById(Long id) {
		return articleDao.getArticleById(id);
	}

	public Article showArticle(Long id) {
		return getArticleById(id);
	}
	
	public Article showArticle(String id) {
		try {
			new Long(id);
			return showArticle(new Long(id));
		} catch (java.lang.NumberFormatException e) {
			return articleDao.getArticleByTitle(id);
		}
	}

	/*
	 * ckeditor adds (ignore) all sorts of unwanted markup, remove all unwanted markup in this filter
	 */
	public Article showFilteredArticle(String id) {
		if (id == null) {
			return new Article();
		}
		Article article = showArticle(id);
		for (String filter: filters) {
			article.setBody(article.getBody().replaceAll(filter, ""));	
		}
		return article;
	}

	public List<Article> getLatestArticles() {
		return articleDao.getLatestArticles();
	}

	public Article getFeaturedArticle() {
		return articleDao.getFeaturedArticle();
	}
		
	/**
	 * called from the edit button at the bottom of the showArticle page 
	 * @return
	 */
	public String editArticleButtonClick() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String articleId = params.get("articleId");
		Map<String, Object> sessionMapObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		Article article = getArticleById(new Long(articleId));
		sessionMapObj.put("editArticleObject", article);
		return "/admin/editArticle.xhtml?faces-redirect=true";
	}
	
	/**
	 * called when the user saves edits made on the edit article page
	 * 
	 * @return
	 */
	public String editArticle() {
		Map<String, Object> sessionMapObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		Article article = (Article) sessionMapObj.get("editArticleObject");

		if (article.getFeatured()) {
			articleDao.resetFeatured();
		}

		// do some formatting? //article.getBody().replaceAll("\\r|\\n", "");

		Article dbArticle = getArticleById(article.getId());
		archiveExistingArticle(dbArticle);
		
		article.orderPageContentItems();
		
		dbArticle.setIcon(applicationController.getNewArticle().getIcon());
		dbArticle.setTitle(article.getTitle());
		dbArticle.setBody(article.getBody());
		dbArticle.setFeatured(article.getFeatured());
		dbArticle.setDeleted(article.getDeleted());
		dbArticle.setHidden(article.getHidden());
		dbArticle.setArchived(article.getArchived());
		dbArticle.setSubtitle(article.getSubtitle());
		dbArticle.setDateAdded(article.getDateAdded());
		dbArticle.setPageContents(article.getPageContents());

		dbArticle = articleDao.merge(dbArticle);
		
		return "/showArticle.xhtml?article=" + article.getId() + "&faces-redirect=true";
	}
	
	private void archiveExistingArticle(Article article) {
		ArticleArchive dbArticleArchive = new ArticleArchive();
		dbArticleArchive.setTitle(article.getTitle());
		dbArticleArchive.setBody(article.getBody());
		dbArticleArchive.setFeatured(article.getFeatured());
		dbArticleArchive.setDeleted(article.getDeleted());
		dbArticleArchive.setArchived(article.getArchived());
		dbArticleArchive.setSubtitle(article.getSubtitle());
		dbArticleArchive.setDateAdded(article.getDateAdded());
		genericDao.persist(dbArticleArchive);
	}
	
	public void archiveArticle() { 
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(); 
		String articleId = params.get("articleId"); 
		archiveExistingArticle(getArticleById(new Long(articleId))); 
		//return "/editArticle.xhtml?faces-redirect=true"; 
	}

	public String addNewArticle() {
		if (applicationController.getNewArticle().getFeatured()) {
			articleDao.resetFeatured();
		}
		
		//reset the associated tags
		applicationController.getNewArticle().setTags(new ArrayList<>());
		List<Tag> tags = tagController.persistTags(formTags);
		applicationController.getNewArticle().setTags(tags);
		
		applicationController.getNewArticle().setArchived(false);
		applicationController.getNewArticle().setDateAdded(new Date());

		applicationController.getNewArticle().orderPageContentItems();
		
		
		for (PageContents p : applicationController.getNewArticle().getPageContents()) {
			p.setArticle(applicationController.getNewArticle());
		}
		
		articleDao.persist(applicationController.getNewArticle());
		Long articleId = applicationController.getNewArticle().getId();

		applicationController.setNewArticle(new Article());
		
		formTags = new ArrayList<>();
		
		FacesMessage message = new FacesMessage("Succesful", "New article created!");
        FacesContext.getCurrentInstance().addMessage(null, message);
        
        return "/showArticle.xhtml?article=" + articleId + "&faces-redirect=true";
	}
	
	public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploadedFile = event.getFile();
        String fileName = uploadedFile.getFileName();
        byte[] contents = uploadedFile.getContents();
        applicationController.getNewArticle().setIcon(Base64.getEncoder().encodeToString(contents));
        FacesMessage msg = new FacesMessage("Succesful", fileName + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    
	public Article getNewArticle() {
		return applicationController.getNewArticle();
	}

	public void setNewArticle(Article newArticle) {
		this.applicationController.setNewArticle(newArticle);
	}

	public ApplicationController getApplicationController() {
		return applicationController;
	}

	public void setApplicationController(ApplicationController applicationController) {
		this.applicationController = applicationController;
	}

	public List<String> getFormTags() {
		return formTags;
	}

	public void setFormTags(List<String> formTags) {
		this.formTags = formTags;
	}
}