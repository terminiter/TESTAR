/******************************************************************************************
 * COPYRIGHT:                                                                             *
 * Universitat Politecnica de Valencia 2013                                               *
 * Camino de Vera, s/n                                                                    *
 * 46022 Valencia, Spain                                                                  *
 * www.upv.es                                                                             *
 *                                                                                        * 
 * D I S C L A I M E R:                                                                   *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)      *
 * in the context of the european funded FITTEST project (contract number ICT257574)      *
 * of which the UPV is the coordinator. As the sole developer of this source code,        *
 * following the signed FITTEST Consortium Agreement, the UPV should decide upon an       *
 * appropriate license under which the source code will be distributed after termination  *
 * of the project. Until this time, this code can be used by the partners of the          *
 * FITTEST project for executing the tasks that are outlined in the Description of Work   *
 * (DoW) that is annexed to the contract with the EU.                                     *
 *                                                                                        * 
 * Although it has already been decided that this code will be distributed under an open  *
 * source license, the exact license has not been decided upon and will be announced      *
 * before the end of the project. Beware of any restrictions regarding the use of this    *
 * work that might arise from the open source license it might fall under! It is the      *
 * UPV's intention to make this work accessible, free of any charge.                      *
 *****************************************************************************************/

/**
 *  @author Sebastian Bauersfeld
 */
package org.fruit.alayer;

import org.fruit.Assert;
import org.fruit.Pair;

public final class TextVisualizer implements Visualizer {
	
	private static final long serialVersionUID = 9156304220974950751L;
	final Position pos;
	final String text;
	final Pen pen;
	
	public TextVisualizer(Position pos, String text, Pen pen){
		Assert.notNull(pos, text, pen);
		this.pos = pos;
		this.text = text;
		this.pen = pen;
	}
	
	public void run(State state, Canvas cv, Pen pen) {
		Assert.notNull(state, cv, pen);
		pen = Pen.merge(pen, this.pen);
		try { // by urueda
			Point p = pos.apply(state);
			Pair<Double, Double> m = cv.textMetrics(pen, text);
			cv.text(pen, p.x() - m.left() / 2, p.y() - m.right() / 2, 0, text);
		} catch (PositionException pe) {}			
	}
}