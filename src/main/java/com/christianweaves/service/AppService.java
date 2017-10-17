package com.christianweaves.service;

import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import com.christianweaves.entities.Article;

@Singleton
public class AppService {
	
    // Injected database connection:
	@PersistenceContext(unitName = "christianweavesDS", type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	private Article featuredArticle;
	private Collection<Article> latestArticles;

	@PostConstruct
    public void init() {
        setLatestArticles(getArticles(10));
        setFeaturedArticle(getTheFeaturedArticle());
    }
	
	/**
	 * retrieve the latest count of articles from the database
	 * 
	 * @return
	 */
	public List<Article> getArticles(int count) {
		Query query = entityManager.createQuery("from Article article");
		query.setFirstResult(0);
		query.setMaxResults(count);
		return query.getResultList();
	}

	/**
	 * return the featured article from the database, featured article indicated by featured attribute
	 * in the article attribute equalling true
	 * @return
	 */
	public Article getTheFeaturedArticle() {
		Query query = entityManager.createQuery("from Article where featured = :boolType");
		query.setParameter("boolType", Boolean.TRUE);
		List<Article> list = query.getResultList();
		return list.get(0);
	}

	/**
	 * return the article based on id
	 * @return
	 */
	public Article getArticleById(Long id) {
		if (id == null) return null;
		Query query = entityManager.createQuery("from Article where id = :id");
		query.setParameter("id", id);
		return (Article)query.getSingleResult();
	}
	
	public void createArticle(String title, String subtitle, String body) {
	
		Article article = new Article();
		article.setTitle(title);
		article.setSubtitle(subtitle);
		article.setBody(body);
		
		entityManager.persist(article);
	}

	public Collection<Article> getLatestArticles() {
		return latestArticles;
	}

	public void setLatestArticles(Collection<Article> latestArticles) {
		this.latestArticles = latestArticles;
	}

	public Article getFeaturedArticle() {
		return featuredArticle;
	}

	public void setFeaturedArticle(Article featuredArticle) {
		this.featuredArticle = featuredArticle;
	}
}
