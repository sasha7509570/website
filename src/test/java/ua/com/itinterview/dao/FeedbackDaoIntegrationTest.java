package ua.com.itinterview.dao;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ua.com.itinterview.entity.FeedbackEntity;

import com.github.stokito.gag.annotation.remark.Facepalm;
import com.github.stokito.gag.annotation.remark.WTF;

public class FeedbackDaoIntegrationTest extends
	BaseEntityWithIdDaoIntegrationTest<FeedbackEntity> {

    @Autowired
    private FeedbackDao feedbackDao;

    @Override
    protected FeedbackEntity createEntity() {
	FeedbackEntity entity = new FeedbackEntity();
	return entity;
    }

    @Override
    protected EntityWithIdDao<FeedbackEntity> getEntityWithIdDao() {
	return feedbackDao;
    }

    @Facepalm
    @Test
    public void testGetAllUncheckedFeedbacks() {
	DateFormat formatter;
	Date date;
	formatter = new SimpleDateFormat("yyyy-MM-dd");
	FeedbackEntity entity = createEntity();

	System.out
		.println("****************************************************");
	List<FeedbackEntity> list2 = feedbackDao.getAllUncheckedFeedbacks();
	System.out
		.println("****************************************************");

	String str_date = "2012-12-07";
	try {
	    date = (Date) formatter.parse(str_date);
	    java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
	    entity.setCreateTime(timeStampDate);
	} catch (ParseException e) {
	    e.printStackTrace();
	}

	entity.setFeedbackText("hhhghghg");
	entity.setChecked(true);
	entity = feedbackDao.save(entity);

	FeedbackEntity entity1 = createEntity();
	str_date = "2012-12-20";
	try {
	    date = (Date) formatter.parse(str_date);
	    java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
	    entity1.setCreateTime(timeStampDate);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	entity1.setFeedbackText("hhhghghg22222");
	entity1.setChecked(false);
	entity1 = feedbackDao.save(entity1);

	FeedbackEntity entity2 = createEntity();
	str_date = "2012-12-25";
	try {
	    date = (Date) formatter.parse(str_date);
	    java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
	    entity2.setCreateTime(timeStampDate);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	entity2.setFeedbackText("hhhghghg33333");
	entity2.setChecked(true);
	entity2 = feedbackDao.save(entity2);

	FeedbackEntity entity3 = createEntity();
	str_date = "2013-01-20";
	formatter = new SimpleDateFormat("yyyy-MM-dd");
	try {
	    date = (Date) formatter.parse(str_date);
	    java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
	    entity3.setCreateTime(timeStampDate);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	entity3.setFeedbackText("hhhghghg4444");
	entity3.setChecked(false);
	entity3 = feedbackDao.save(entity3);

	List<FeedbackEntity> list = feedbackDao.getAllUncheckedFeedbacks();
	assertEquals(2, list.size());
	System.out.println(list.get(0).toString());
	System.out.println(list.get(1).toString());

	assertEquals(entity1, list.get(0));
	assertEquals(entity3, list.get(1));
    }

    @WTF
    @Test
    public void testGetFeedbacksForPeriod() throws ParseException {
	DateFormat formatter;
	Date date, dateFrom, dateTo;

	String str_date = "2012-12-24";
	formatter = new SimpleDateFormat("yyyy-MM-dd");
	dateFrom = (Date) formatter.parse(str_date);

	str_date = "2012-12-26";
	dateTo = (Date) formatter.parse(str_date);

	System.out
		.println("****************************************************");
	List<FeedbackEntity> list = feedbackDao.getFeedbacksForPeriod(dateFrom,
		dateTo);
	System.out
		.println("****************************************************");
	FeedbackEntity entity = createEntity();
	str_date = "2012-12-07";
	try {
	    date = (Date) formatter.parse(str_date);
	    java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
	    entity.setCreateTime(timeStampDate);
	} catch (ParseException e) {
	    e.printStackTrace();
	}

	entity.setFeedbackText("hhhghghg000");
	entity.setChecked(true);
	entity = feedbackDao.save(entity);

	FeedbackEntity entity1 = createEntity();
	str_date = "2012-12-20";
	try {
	    date = (Date) formatter.parse(str_date);

	    java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
	    entity1.setCreateTime(timeStampDate);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	entity1.setFeedbackText("hhhghghg1111");
	entity1.setChecked(false);
	entity1 = feedbackDao.save(entity1);

	FeedbackEntity entity2 = createEntity();
	str_date = "2012-12-25";
	try {
	    date = (Date) formatter.parse(str_date);
	    java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
	    entity2.setCreateTime(timeStampDate);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	entity2.setFeedbackText("hhhghghg22222");
	entity2.setChecked(true);
	entity2 = feedbackDao.save(entity2);

	FeedbackEntity entity3 = createEntity();
	str_date = "2013-01-20";
	try {
	    date = (Date) formatter.parse(str_date);
	    java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
	    entity3.setCreateTime(timeStampDate);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	entity3.setFeedbackText("hhhghghg333333");
	entity3.setChecked(false);
	entity3 = feedbackDao.save(entity3);

	List<FeedbackEntity> list2 = feedbackDao.getFeedbacksForPeriod(
		dateFrom, dateTo);
	assertEquals(1, list2.size());
	assertEquals(entity2, list2.get(0));

    }

}
