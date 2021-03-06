//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clStatManager
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong, Kim Hyun Woo
//  @ Email : rkdtlsdnr102@naver.com

package org.androidtown.sleeper.endclasses;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clStatManager.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong
//  @ Email : rkdtlsdnr102@naver.com
//
//
// Copyright (C) 2015  Kang Shin Wook, Kim Hyun Woong
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License along
//  with this program; if not, write to the Free Software Foundation, Inc.,
//  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.List;

/**
 * Statmanager is interface class between object that collects data and view that displays statistics
 * using those collected data. StatManager needs GraphView 4.0 version.
 *
 * In our app, an object that collects data should create graph and put in StatManager instance. Then
 * in StatisticManageFragment displays statistic in the format that we set here: graphList is graph
 * that is displayed. There is scroll bar in StatisticManageFragment, which enable you to see y value at
 * specific x value. Data that change over scroll movement is ,as we call, variable data. But sometimes
 * we want to display static data, for example wake time, sleep time. So we made property staticDataList
 * so that developer can put static data. They are displayed in order that data were put in staticDataList.
 *
 */
public class clStatManager {
	private List<GraphView> graphList=null;
	private List<String> staticData=null;
	private List<String> staticDataName=null;
	private List<double[]> xDataList =null ;
	private List<Integer> dataSizeList=null ;

	/**
	 * Constructor
	 */
	public clStatManager() {

		graphList=new ArrayList<>() ;
		staticData=new ArrayList<>() ;
		staticDataName=new ArrayList<>() ;
		xDataList=new ArrayList<>() ;
		dataSizeList=new ArrayList<>() ;
	}

	/**
	 * Add graph to display
	 * @param graph graph to display
	 */
	public void addGraph(GraphView graph,int dataSize) {

		graphList.add(graph) ;
		dataSizeList.add(dataSize) ;

	}

	/**
	 * Get graph list
	 * @return graphlist that user defined
	 */
	public List getGraphList() {

		return graphList ;
	}
	
	public void addStaticData(String dataName, String data) {

		staticDataName.add(dataName) ;
		staticData.add(data) ;
	}
	
	public List getStaticDataList() {

		return staticData ;
	}

	public List<Integer> getDataSizeList(){

		return  dataSizeList;
	}

	public List getStaticDataNameList(){

		return staticDataName ;
	}

	public void addXData(double[] data){

		xDataList.add(data) ;
	}

	public List<double[]> getXDataList(){

		return xDataList;
	}
}
