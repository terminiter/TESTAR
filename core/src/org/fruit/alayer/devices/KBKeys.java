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
package org.fruit.alayer.devices;

public enum KBKeys {
	KEY_FIRST(400), KEY_LAST(402), KEY_LOCATION_LEFT(2), KEY_LOCATION_NUMPAD(4), KEY_LOCATION_RIGHT(3), KEY_LOCATION_STANDARD(1),
	KEY_LOCATION_UNKNOWN(0), KEY_PRESSED(401), KEY_RELEASED(402), KEY_TYPED(400), VK_0(48), VK_1(49), VK_2(50), VK_3(51), VK_4(52),
	VK_5(53), VK_6(54), VK_7(55), VK_8(56), VK_9(57), VK_A(65), VK_ACCEPT(30), VK_ADD(107), VK_AGAIN(65481), VK_ALL_CANDIDATES(256),
	VK_ALPHANUMERIC(240), VK_ALT(18), VK_ALT_GRAPH(65406), VK_AMPERSAND(150), /*VK_ASTERISK(151),*/ VK_AT(512), VK_B(66), VK_BACK_QUOTE(192),
	VK_BACK_SLASH(92), VK_BACK_SPACE(8), VK_BRACELEFT(161), VK_BRACERIGHT(162), VK_C(67), VK_CANCEL(3), VK_CAPS_LOCK(20), VK_CIRCUMFLEX(514),
	VK_CLEAR(12), VK_CLOSE_BRACKET(93), VK_CODE_INPUT(258), VK_COLON(513), VK_COMMA(44), VK_COMPOSE(65312), VK_CONTROL(17), VK_CONVERT(28),
	VK_COPY(65485), VK_CUT(65489), VK_D(68), VK_DEAD_ABOVEDOT(134), VK_DEAD_ABOVERING(136), VK_DEAD_ACUTE(129), VK_DEAD_BREVE(133),
	VK_DEAD_CARON(138), VK_DEAD_CEDILLA(139), VK_DEAD_CIRCUMFLEX(130), VK_DEAD_DIAERESIS(135), VK_DEAD_DOUBLEACUTE(137), VK_DEAD_GRAVE(128),
	VK_DEAD_IOTA(141), VK_DEAD_MACRON(132), VK_DEAD_OGONEK(140), VK_DEAD_SEMIVOICED_SOUND(143), VK_DEAD_TILDE(131), VK_DEAD_VOICED_SOUND(142),
	VK_DECIMAL(110), VK_DELETE(127), VK_DIVIDE(111), VK_DOLLAR(515), VK_DOWN(40), VK_E(69), VK_END(35), VK_ENTER(10), VK_EQUALS(61),
	VK_ESCAPE(27), VK_EURO_SIGN(516), /*VK_EXCLAMATION_MARK(517),*/ VK_F(70), VK_F1(112), VK_F10(121), VK_F11(122), VK_F12(123), VK_F13(61440),
	VK_F14(61441), VK_F15(61442), VK_F16(61443), VK_F17(61444), VK_F18(61445), VK_F19(61446), VK_F2(113), VK_F20(61447), VK_F21(61448),
	VK_F22(61449), VK_F23(61450), VK_F24(61451), VK_F3(114), VK_F4(115), VK_F5(116), VK_F6(117), VK_F7(118), VK_F8(119), VK_F9(120),
	VK_FINAL(24), VK_FIND(65488), VK_FULL_WIDTH(243), VK_G(71), VK_GREATER(160), VK_H(72), VK_HALF_WIDTH(244), VK_HELP(156), VK_HIRAGANA(242),
	VK_HOME(36), VK_I(73), VK_INPUT_METHOD_ON_OFF(263), VK_INSERT(155), VK_INVERTED_EXCLAMATION_MARK(518), VK_J(74), VK_JAPANESE_HIRAGANA(260),
	VK_JAPANESE_KATAKANA(259), VK_JAPANESE_ROMAN(261), VK_K(75), VK_KANA(21), VK_KANA_LOCK(262), VK_KANJI(25), VK_KATAKANA(241), VK_KP_DOWN(225),
	VK_KP_LEFT(226), VK_KP_RIGHT(227), VK_KP_UP(224), VK_L(76), VK_LEFT(37), VK_LEFT_PARENTHESIS(519), VK_LESS(153), VK_M(77), VK_META(157),
	VK_MINUS(45), VK_MODECHANGE(31), VK_MULTIPLY(106), VK_N(78), VK_NONCONVERT(29), VK_NUM_LOCK(144), VK_NUMBER_SIGN(520), VK_NUMPAD0(96),
	VK_NUMPAD1(97), VK_NUMPAD2(98), VK_NUMPAD3(99), VK_NUMPAD4(100), VK_NUMPAD5(101), VK_NUMPAD6(102), VK_NUMPAD7(103), VK_NUMPAD8(104),
	VK_NUMPAD9(105), VK_O(79), VK_OPEN_BRACKET(91), VK_P(80), VK_PAGE_DOWN(34), VK_PAGE_UP(33), VK_PASTE(65487), VK_PAUSE(19), VK_PERIOD(46),
	VK_PLUS(521), VK_PREVIOUS_CANDIDATE(257), VK_PRINTSCREEN(154), VK_PROPS(65482), VK_Q(81), VK_QUOTE(222), VK_QUOTEDBL(152), VK_R(82),
	VK_RIGHT(39), VK_RIGHT_PARENTHESIS(522), VK_ROMAN_CHARACTERS(245), VK_S(83), VK_SCROLL_LOCK(145), VK_SEMICOLON(59), VK_SEPARATER(108),
	VK_SEPARATOR(108), VK_SHIFT(16), VK_SLASH(47), VK_SPACE(32), VK_STOP(65480), VK_SUBTRACT(109), VK_T(84), VK_TAB(9), VK_U(85), VK_UNDEFINED(0),
	VK_UNDERSCORE(523), VK_UNDO(65483), VK_UP(38), VK_V(86), VK_W(87), VK_X(88), VK_Y(89), VK_Z(90),
	VK_ASTERISK(42), VK_ARROBA(64), VK_EXCLAMATION_MARK(33); // by urueda

	private final int code;
	private KBKeys(int code){ this.code = code; }
	public int code(){ return code; }
	
	// by urueda
	public static boolean contains(String s) {
		try {
			KBKeys.valueOf(s);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
}