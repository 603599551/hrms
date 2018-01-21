package com.utils;


/**
 * 封装分页的操作
 * 
 * 开始准备查询时，需要传入当前页数，每页显示的总共条数。查询时会自动计算出从第几条记录开始查询
 * 
 * 查询后，需要传入总共的记录数。会自动计算出总共页数
 * 
 */
public class TurnPage {

	/**
	 * 当前页面显示的条数
	 */
	private int rowInOnePage = 10;

	/**
	 * 数据的总共数
	 * 作用：参与计算总共的页数
	 */
	private long total;

	/**
	 * 当前页数
	 */
	private int pageNum = 1;

	/**
	 * 总共页数
	 * 用于页面的显示
	 */
	private int pageCount = 0;
	
	/**
	 * 执行查询时，开始的记录数
	 */
	private int startNum=0;
	/**
	 * 
	 */
	public TurnPage() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return Returns the pageCount.
	 * 计算总共的页数
	 */
	public int getPageCount() {
		long tmp = total/(new Long(rowInOnePage)).longValue();
		pageCount = (new Long(tmp)).intValue();
		if((total%rowInOnePage)>0)
			pageCount++;
		return pageCount;
	}
	/**
	 * @param pageCount The pageCount to set.
	 */
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	/**
	 * @return Returns the pageNum.
	 */
	public int getPageNum() {
		return pageNum;
	}
	/**
	 * @param pageNum The pageNum to set.
	 */
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	/**
	 * @return Returns the rowInOnePage.
	 */
	public int getRowInOnePage() {
		return rowInOnePage;
	}
	/**
	 * @param rowInOnePage The rowInOnePage to set.
	 */
	public void setRowInOnePage(int rowInOnePage) {
		this.rowInOnePage = rowInOnePage;
	}
	/**
	 * @return Returns the total.
	 */
	public long getTotal() {
		return total;
	}
	/**
	 * @param total The total to set.
	 */
	public void setTotal(long total) {
		this.total = total;
	}
	public int getStartNum() {
		startNum=(pageNum-1)*rowInOnePage;
		return startNum;
	}

}
