package com.dci.intellij.dbn.language.psql.dialect.oracle;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;

%%

%class OraclePLSQLParserFlexLexer
%implements FlexLexer
%public
%pack
%final
%unicode
%ignorecase
%function advance
%type IElementType
%eof{ return;
%eof}

%{
    private int braceCounter = 0;
    private TokenTypeBundle tt;
    public OraclePLSQLParserFlexLexer(TokenTypeBundle tt) {
        this.tt = tt;
    }
%}

WHITE_SPACE= {white_space_char}|{line_terminator}
line_terminator = \r|\n|\r\n
input_character = [^\r\n]
white_space = [ \t\f]
white_space_char= [ \n\r\t\f]
ws  = {WHITE_SPACE}+
wso = {WHITE_SPACE}*

comment_tail =([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
BLOCK_COMMENT=("/*"[^]{comment_tail})|"/*"
LINE_COMMENT = "--" {input_character}*
REM_LINE_COMMENT = "rem"({white_space}+{input_character}*|{line_terminator})

IDENTIFIER = [:jletter:] ([:jletterdigit:]|"#")*
QUOTED_IDENTIFIER = "\""[^\"]*"\""?

string_simple_quoted      = "'"([^\']|"''"|"\\'")*"'"?
string_alternative_quoted = "q'["[^\[\]]*"]'"? | "q'("[^\(\)]*")'"? | "q'{"[^\{\}]*"}'"? | "q'!"[^\!]*"!'"? | "q'<"[^\<\>]*">'"? | "q'|"[^|]*"|'"?
STRING = "n"?({string_alternative_quoted}|{string_simple_quoted})

sign = "+"|"-"
digit = [0-9]
INTEGER = {digit}+("e"{sign}?{digit}+)?
NUMBER = {INTEGER}?"."{digit}+(("e"{sign}?{digit}+)|(("f"|"d"){ws}))?

VARIABLE = ":"{INTEGER}
SQLP_VARIABLE = "&""&"?{IDENTIFIER}

%state PLSQL, WRAPPED
%%

<WRAPPED> {
    .*               { return tt.getSharedTokenTypes().getLineComment(); }
}


{WHITE_SPACE}+   { return tt.getSharedTokenTypes().getWhiteSpace(); }

{BLOCK_COMMENT}      { return tt.getSharedTokenTypes().getBlockComment(); }
{LINE_COMMENT}       { return tt.getSharedTokenTypes().getLineComment(); }
{REM_LINE_COMMENT}   { return tt.getSharedTokenTypes().getLineComment(); }

"wrapped"            { yybegin(WRAPPED); return tt.getSharedTokenTypes().getBlockComment();}

{VARIABLE}          {return tt.getSharedTokenTypes().getVariable(); }
{SQLP_VARIABLE}     {return tt.getSharedTokenTypes().getVariable(); }


{INTEGER}     { return tt.getSharedTokenTypes().getInteger(); }
{NUMBER}      { return tt.getSharedTokenTypes().getNumber(); }
{STRING}      { return tt.getSharedTokenTypes().getString(); }

"("{wso}"+"{wso}")"  {return tt.getTokenType("CT_OUTER_JOIN");}

"||" {return tt.getOperatorTokenType(0);}
":=" {return tt.getOperatorTokenType(1);}
".." {return tt.getOperatorTokenType(2);}

"@" {return tt.getCharacterTokenType(0);}
":" {return tt.getCharacterTokenType(1);}
"," {return tt.getCharacterTokenType(2);}
"." {return tt.getCharacterTokenType(3);}
"=" {return tt.getCharacterTokenType(4);}
"!" {return tt.getCharacterTokenType(5);}
">" {return tt.getCharacterTokenType(6);}
"#" {return tt.getCharacterTokenType(7);}
"[" {return tt.getCharacterTokenType(8);}
"{" {return tt.getCharacterTokenType(9);}
"(" {return tt.getCharacterTokenType(10);}
"<" {return tt.getCharacterTokenType(11);}
"-" {return tt.getCharacterTokenType(12);}
"%" {return tt.getCharacterTokenType(13);}
"+" {return tt.getCharacterTokenType(14);}
"]" {return tt.getCharacterTokenType(15);}
"}" {return tt.getCharacterTokenType(16);}
")" {return tt.getCharacterTokenType(17);}
";" {return tt.getCharacterTokenType(18);}
"/" {return tt.getCharacterTokenType(19);}
"*" {return tt.getCharacterTokenType(20);}
"|" {return tt.getCharacterTokenType(21);}




"varchar2" {return tt.getDataTypeTokenType(0);}
"with"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(1);}
"with"{ws}"local"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(2);}
"varchar" {return tt.getDataTypeTokenType(3);}
"urowid" {return tt.getDataTypeTokenType(4);}
"to"{ws}"second" {return tt.getDataTypeTokenType(5);}
"to"{ws}"month" {return tt.getDataTypeTokenType(6);}
"timestamp" {return tt.getDataTypeTokenType(7);}
"string" {return tt.getDataTypeTokenType(8);}
"smallint" {return tt.getDataTypeTokenType(9);}
"rowid" {return tt.getDataTypeTokenType(10);}
"real" {return tt.getDataTypeTokenType(11);}
"raw" {return tt.getDataTypeTokenType(12);}
"pls_integer" {return tt.getDataTypeTokenType(13);}
"nvarchar2" {return tt.getDataTypeTokenType(14);}
"numeric" {return tt.getDataTypeTokenType(15);}
"number" {return tt.getDataTypeTokenType(16);}
"nclob" {return tt.getDataTypeTokenType(17);}
"nchar"{ws}"varying" {return tt.getDataTypeTokenType(18);}
"nchar" {return tt.getDataTypeTokenType(19);}
"national"{ws}"character"{ws}"varying" {return tt.getDataTypeTokenType(20);}
"national"{ws}"character" {return tt.getDataTypeTokenType(21);}
"national"{ws}"char"{ws}"varying" {return tt.getDataTypeTokenType(22);}
"national"{ws}"char" {return tt.getDataTypeTokenType(23);}
"long"{ws}"varchar" {return tt.getDataTypeTokenType(24);}
"long"{ws}"raw" {return tt.getDataTypeTokenType(25);}
"long" {return tt.getDataTypeTokenType(26);}
"interval"{ws}"year" {return tt.getDataTypeTokenType(27);}
"interval"{ws}"day" {return tt.getDataTypeTokenType(28);}
"integer" {return tt.getDataTypeTokenType(29);}
"int" {return tt.getDataTypeTokenType(30);}
"float" {return tt.getDataTypeTokenType(31);}
"double"{ws}"precision" {return tt.getDataTypeTokenType(32);}
"decimal" {return tt.getDataTypeTokenType(33);}
"date" {return tt.getDataTypeTokenType(34);}
"clob" {return tt.getDataTypeTokenType(35);}
"character"{ws}"varying" {return tt.getDataTypeTokenType(36);}
"character" {return tt.getDataTypeTokenType(37);}
"char" {return tt.getDataTypeTokenType(38);}
"byte" {return tt.getDataTypeTokenType(39);}
"boolean" {return tt.getDataTypeTokenType(40);}
"blob" {return tt.getDataTypeTokenType(41);}
"binary_integer" {return tt.getDataTypeTokenType(42);}
"binary_float" {return tt.getDataTypeTokenType(43);}
"binary_double" {return tt.getDataTypeTokenType(44);}
"bfile" {return tt.getDataTypeTokenType(45);}




"a set" {return tt.getKeywordTokenType(0);}
"after" {return tt.getKeywordTokenType(1);}
"agent" {return tt.getKeywordTokenType(2);}
"all" {return tt.getKeywordTokenType(3);}
"alter" {return tt.getKeywordTokenType(4);}
"analyze" {return tt.getKeywordTokenType(5);}
"and" {return tt.getKeywordTokenType(6);}
"any" {return tt.getKeywordTokenType(7);}
"array" {return tt.getKeywordTokenType(8);}
"as" {return tt.getKeywordTokenType(9);}
"asc" {return tt.getKeywordTokenType(10);}
"associate" {return tt.getKeywordTokenType(11);}
"at" {return tt.getKeywordTokenType(12);}
"audit" {return tt.getKeywordTokenType(13);}
"authid" {return tt.getKeywordTokenType(14);}
"automatic" {return tt.getKeywordTokenType(15);}
"autonomous_transaction" {return tt.getKeywordTokenType(16);}
"before" {return tt.getKeywordTokenType(17);}
"begin" {return tt.getKeywordTokenType(18);}
"between" {return tt.getKeywordTokenType(19);}
"block" {return tt.getKeywordTokenType(20);}
"body" {return tt.getKeywordTokenType(21);}
"both" {return tt.getKeywordTokenType(22);}
"bulk" {return tt.getKeywordTokenType(23);}
"bulk_exceptions" {return tt.getKeywordTokenType(24);}
"bulk_rowcount" {return tt.getKeywordTokenType(25);}
"by" {return tt.getKeywordTokenType(26);}
"c" {return tt.getKeywordTokenType(27);}
"call" {return tt.getKeywordTokenType(28);}
"canonical" {return tt.getKeywordTokenType(29);}
"case" {return tt.getKeywordTokenType(30);}
"char_base" {return tt.getKeywordTokenType(31);}
"char_cs" {return tt.getKeywordTokenType(32);}
"charsetform" {return tt.getKeywordTokenType(33);}
"charsetid" {return tt.getKeywordTokenType(34);}
"check" {return tt.getKeywordTokenType(35);}
"chisq_df" {return tt.getKeywordTokenType(36);}
"chisq_obs" {return tt.getKeywordTokenType(37);}
"chisq_sig" {return tt.getKeywordTokenType(38);}
"close" {return tt.getKeywordTokenType(39);}
"cluster" {return tt.getKeywordTokenType(40);}
"coalesce" {return tt.getKeywordTokenType(41);}
"coefficient" {return tt.getKeywordTokenType(42);}
"cohens_k" {return tt.getKeywordTokenType(43);}
"collect" {return tt.getKeywordTokenType(44);}
"columns" {return tt.getKeywordTokenType(45);}
"comment" {return tt.getKeywordTokenType(46);}
"commit" {return tt.getKeywordTokenType(47);}
"committed" {return tt.getKeywordTokenType(48);}
"compatibility" {return tt.getKeywordTokenType(49);}
"compound" {return tt.getKeywordTokenType(50);}
"compress" {return tt.getKeywordTokenType(51);}
"connect" {return tt.getKeywordTokenType(52);}
"constant" {return tt.getKeywordTokenType(53);}
"constraint" {return tt.getKeywordTokenType(54);}
"constructor" {return tt.getKeywordTokenType(55);}
"cont_coefficient" {return tt.getKeywordTokenType(56);}
"content" {return tt.getKeywordTokenType(57);}
"context" {return tt.getKeywordTokenType(58);}
"count" {return tt.getKeywordTokenType(59);}
"cramers_v" {return tt.getKeywordTokenType(60);}
"create" {return tt.getKeywordTokenType(61);}
"cross" {return tt.getKeywordTokenType(62);}
"cube" {return tt.getKeywordTokenType(63);}
"current" {return tt.getKeywordTokenType(64);}
"current_user" {return tt.getKeywordTokenType(65);}
"currval" {return tt.getKeywordTokenType(66);}
"cursor" {return tt.getKeywordTokenType(67);}
"database" {return tt.getKeywordTokenType(68);}
"day" {return tt.getKeywordTokenType(69);}
"db_role_change" {return tt.getKeywordTokenType(70);}
"ddl" {return tt.getKeywordTokenType(71);}
"declare" {return tt.getKeywordTokenType(72);}
"decrement" {return tt.getKeywordTokenType(73);}
"default" {return tt.getKeywordTokenType(74);}
"defaults" {return tt.getKeywordTokenType(75);}
"definer" {return tt.getKeywordTokenType(76);}
"delete" {return tt.getKeywordTokenType(77);}
"deleting" {return tt.getKeywordTokenType(78);}
"dense_rank" {return tt.getKeywordTokenType(79);}
"desc" {return tt.getKeywordTokenType(80);}
"deterministic" {return tt.getKeywordTokenType(81);}
"df" {return tt.getKeywordTokenType(82);}
"df_between" {return tt.getKeywordTokenType(83);}
"df_den" {return tt.getKeywordTokenType(84);}
"df_num" {return tt.getKeywordTokenType(85);}
"df_within" {return tt.getKeywordTokenType(86);}
"dimension" {return tt.getKeywordTokenType(87);}
"disable" {return tt.getKeywordTokenType(88);}
"disassociate" {return tt.getKeywordTokenType(89);}
"distinct" {return tt.getKeywordTokenType(90);}
"do" {return tt.getKeywordTokenType(91);}
"document" {return tt.getKeywordTokenType(92);}
"drop" {return tt.getKeywordTokenType(93);}
"dump" {return tt.getKeywordTokenType(94);}
"duration" {return tt.getKeywordTokenType(95);}
"each" {return tt.getKeywordTokenType(96);}
"else" {return tt.getKeywordTokenType(97);}
"elsif" {return tt.getKeywordTokenType(98);}
"empty" {return tt.getKeywordTokenType(99);}
"enable" {return tt.getKeywordTokenType(100);}
"encoding" {return tt.getKeywordTokenType(101);}
"end" {return tt.getKeywordTokenType(102);}
"entityescaping" {return tt.getKeywordTokenType(103);}
"equals_path" {return tt.getKeywordTokenType(104);}
"error_code" {return tt.getKeywordTokenType(105);}
"error_index" {return tt.getKeywordTokenType(106);}
"errors" {return tt.getKeywordTokenType(107);}
"escape" {return tt.getKeywordTokenType(108);}
"evalname" {return tt.getKeywordTokenType(109);}
"exact_prob" {return tt.getKeywordTokenType(110);}
"except" {return tt.getKeywordTokenType(111);}
"exception" {return tt.getKeywordTokenType(112);}
"exception_init" {return tt.getKeywordTokenType(113);}
"exceptions" {return tt.getKeywordTokenType(114);}
"exclude" {return tt.getKeywordTokenType(115);}
"exclusive" {return tt.getKeywordTokenType(116);}
"execute" {return tt.getKeywordTokenType(117);}
"exists" {return tt.getKeywordTokenType(118);}
"exit" {return tt.getKeywordTokenType(119);}
"extend" {return tt.getKeywordTokenType(120);}
"extends" {return tt.getKeywordTokenType(121);}
"external" {return tt.getKeywordTokenType(122);}
"f_ratio" {return tt.getKeywordTokenType(123);}
"fetch" {return tt.getKeywordTokenType(124);}
"final" {return tt.getKeywordTokenType(125);}
"first" {return tt.getKeywordTokenType(126);}
"following" {return tt.getKeywordTokenType(127);}
"follows" {return tt.getKeywordTokenType(128);}
"for" {return tt.getKeywordTokenType(129);}
"forall" {return tt.getKeywordTokenType(130);}
"force" {return tt.getKeywordTokenType(131);}
"found" {return tt.getKeywordTokenType(132);}
"from" {return tt.getKeywordTokenType(133);}
"full" {return tt.getKeywordTokenType(134);}
"function" {return tt.getKeywordTokenType(135);}
"goto" {return tt.getKeywordTokenType(136);}
"grant" {return tt.getKeywordTokenType(137);}
"group" {return tt.getKeywordTokenType(138);}
"having" {return tt.getKeywordTokenType(139);}
"heap" {return tt.getKeywordTokenType(140);}
"hide" {return tt.getKeywordTokenType(141);}
"hour" {return tt.getKeywordTokenType(142);}
"if" {return tt.getKeywordTokenType(143);}
"ignore" {return tt.getKeywordTokenType(144);}
"immediate" {return tt.getKeywordTokenType(145);}
"in" {return tt.getKeywordTokenType(146);}
"in"{ws}"out" {return tt.getKeywordTokenType(147);}
"include" {return tt.getKeywordTokenType(148);}
"increment" {return tt.getKeywordTokenType(149);}
"indent" {return tt.getKeywordTokenType(150);}
"index" {return tt.getKeywordTokenType(151);}
"indicator" {return tt.getKeywordTokenType(152);}
"indices" {return tt.getKeywordTokenType(153);}
"infinite" {return tt.getKeywordTokenType(154);}
"inline" {return tt.getKeywordTokenType(155);}
"inner" {return tt.getKeywordTokenType(156);}
"insert" {return tt.getKeywordTokenType(157);}
"inserting" {return tt.getKeywordTokenType(158);}
"instantiable" {return tt.getKeywordTokenType(159);}
"instead" {return tt.getKeywordTokenType(160);}
"interface" {return tt.getKeywordTokenType(161);}
"intersect" {return tt.getKeywordTokenType(162);}
"interval" {return tt.getKeywordTokenType(163);}
"into" {return tt.getKeywordTokenType(164);}
"is" {return tt.getKeywordTokenType(165);}
"isolation" {return tt.getKeywordTokenType(166);}
"isopen" {return tt.getKeywordTokenType(167);}
"iterate" {return tt.getKeywordTokenType(168);}
"java" {return tt.getKeywordTokenType(169);}
"join" {return tt.getKeywordTokenType(170);}
"keep" {return tt.getKeywordTokenType(171);}
"language" {return tt.getKeywordTokenType(172);}
"last" {return tt.getKeywordTokenType(173);}
"leading" {return tt.getKeywordTokenType(174);}
"left" {return tt.getKeywordTokenType(175);}
"level" {return tt.getKeywordTokenType(176);}
"library" {return tt.getKeywordTokenType(177);}
"like" {return tt.getKeywordTokenType(178);}
"like2" {return tt.getKeywordTokenType(179);}
"like4" {return tt.getKeywordTokenType(180);}
"likec" {return tt.getKeywordTokenType(181);}
"limit" {return tt.getKeywordTokenType(182);}
"limited" {return tt.getKeywordTokenType(183);}
"local" {return tt.getKeywordTokenType(184);}
"lock" {return tt.getKeywordTokenType(185);}
"log" {return tt.getKeywordTokenType(186);}
"logoff" {return tt.getKeywordTokenType(187);}
"logon" {return tt.getKeywordTokenType(188);}
"loop" {return tt.getKeywordTokenType(189);}
"main" {return tt.getKeywordTokenType(190);}
"map" {return tt.getKeywordTokenType(191);}
"matched" {return tt.getKeywordTokenType(192);}
"maxlen" {return tt.getKeywordTokenType(193);}
"maxvalue" {return tt.getKeywordTokenType(194);}
"mean_squares_between" {return tt.getKeywordTokenType(195);}
"mean_squares_within" {return tt.getKeywordTokenType(196);}
"measures" {return tt.getKeywordTokenType(197);}
"member" {return tt.getKeywordTokenType(198);}
"merge" {return tt.getKeywordTokenType(199);}
"minus" {return tt.getKeywordTokenType(200);}
"minute" {return tt.getKeywordTokenType(201);}
"minvalue" {return tt.getKeywordTokenType(202);}
"mlslabel" {return tt.getKeywordTokenType(203);}
"mode" {return tt.getKeywordTokenType(204);}
"model" {return tt.getKeywordTokenType(205);}
"month" {return tt.getKeywordTokenType(206);}
"multiset" {return tt.getKeywordTokenType(207);}
"name" {return tt.getKeywordTokenType(208);}
"nan" {return tt.getKeywordTokenType(209);}
"natural" {return tt.getKeywordTokenType(210);}
"naturaln" {return tt.getKeywordTokenType(211);}
"nav" {return tt.getKeywordTokenType(212);}
"nchar_cs" {return tt.getKeywordTokenType(213);}
"nested" {return tt.getKeywordTokenType(214);}
"new" {return tt.getKeywordTokenType(215);}
"next" {return tt.getKeywordTokenType(216);}
"nextval" {return tt.getKeywordTokenType(217);}
"no" {return tt.getKeywordTokenType(218);}
"noaudit" {return tt.getKeywordTokenType(219);}
"nocopy" {return tt.getKeywordTokenType(220);}
"nocycle" {return tt.getKeywordTokenType(221);}
"noentityescaping" {return tt.getKeywordTokenType(222);}
"noschemacheck" {return tt.getKeywordTokenType(223);}
"not" {return tt.getKeywordTokenType(224);}
"notfound" {return tt.getKeywordTokenType(225);}
"nowait" {return tt.getKeywordTokenType(226);}
"null" {return tt.getKeywordTokenType(227);}
"nulls" {return tt.getKeywordTokenType(228);}
"number_base" {return tt.getKeywordTokenType(229);}
"object" {return tt.getKeywordTokenType(230);}
"ocirowid" {return tt.getKeywordTokenType(231);}
"of" {return tt.getKeywordTokenType(232);}
"oid" {return tt.getKeywordTokenType(233);}
"old" {return tt.getKeywordTokenType(234);}
"on" {return tt.getKeywordTokenType(235);}
"one_sided_prob_or_less" {return tt.getKeywordTokenType(236);}
"one_sided_prob_or_more" {return tt.getKeywordTokenType(237);}
"one_sided_sig" {return tt.getKeywordTokenType(238);}
"only" {return tt.getKeywordTokenType(239);}
"opaque" {return tt.getKeywordTokenType(240);}
"open" {return tt.getKeywordTokenType(241);}
"operator" {return tt.getKeywordTokenType(242);}
"option" {return tt.getKeywordTokenType(243);}
"or" {return tt.getKeywordTokenType(244);}
"order" {return tt.getKeywordTokenType(245);}
"ordinality" {return tt.getKeywordTokenType(246);}
"organization" {return tt.getKeywordTokenType(247);}
"others" {return tt.getKeywordTokenType(248);}
"out" {return tt.getKeywordTokenType(249);}
"outer" {return tt.getKeywordTokenType(250);}
"over" {return tt.getKeywordTokenType(251);}
"overriding" {return tt.getKeywordTokenType(252);}
"package" {return tt.getKeywordTokenType(253);}
"parallel_enable" {return tt.getKeywordTokenType(254);}
"parameters" {return tt.getKeywordTokenType(255);}
"parent" {return tt.getKeywordTokenType(256);}
"partition" {return tt.getKeywordTokenType(257);}
"passing" {return tt.getKeywordTokenType(258);}
"path" {return tt.getKeywordTokenType(259);}
"pctfree" {return tt.getKeywordTokenType(260);}
"phi_coefficient" {return tt.getKeywordTokenType(261);}
"pipelined" {return tt.getKeywordTokenType(262);}
"pivot" {return tt.getKeywordTokenType(263);}
"positive" {return tt.getKeywordTokenType(264);}
"positiven" {return tt.getKeywordTokenType(265);}
"power" {return tt.getKeywordTokenType(266);}
"pragma" {return tt.getKeywordTokenType(267);}
"preceding" {return tt.getKeywordTokenType(268);}
"present" {return tt.getKeywordTokenType(269);}
"prior" {return tt.getKeywordTokenType(270);}
"private" {return tt.getKeywordTokenType(271);}
"procedure" {return tt.getKeywordTokenType(272);}
"public" {return tt.getKeywordTokenType(273);}
"raise" {return tt.getKeywordTokenType(274);}
"range" {return tt.getKeywordTokenType(275);}
"read" {return tt.getKeywordTokenType(276);}
"record" {return tt.getKeywordTokenType(277);}
"ref" {return tt.getKeywordTokenType(278);}
"reference" {return tt.getKeywordTokenType(279);}
"referencing" {return tt.getKeywordTokenType(280);}
"regexp_like" {return tt.getKeywordTokenType(281);}
"reject" {return tt.getKeywordTokenType(282);}
"release" {return tt.getKeywordTokenType(283);}
"relies_on" {return tt.getKeywordTokenType(284);}
"remainder" {return tt.getKeywordTokenType(285);}
"rename" {return tt.getKeywordTokenType(286);}
"replace" {return tt.getKeywordTokenType(287);}
"restrict_references" {return tt.getKeywordTokenType(288);}
"result" {return tt.getKeywordTokenType(289);}
"result_cache" {return tt.getKeywordTokenType(290);}
"return" {return tt.getKeywordTokenType(291);}
"returning" {return tt.getKeywordTokenType(292);}
"reverse" {return tt.getKeywordTokenType(293);}
"revoke" {return tt.getKeywordTokenType(294);}
"right" {return tt.getKeywordTokenType(295);}
"rnds" {return tt.getKeywordTokenType(296);}
"rnps" {return tt.getKeywordTokenType(297);}
"rollback" {return tt.getKeywordTokenType(298);}
"rollup" {return tt.getKeywordTokenType(299);}
"row" {return tt.getKeywordTokenType(300);}
"rowcount" {return tt.getKeywordTokenType(301);}
"rownum" {return tt.getKeywordTokenType(302);}
"rows" {return tt.getKeywordTokenType(303);}
"rowtype" {return tt.getKeywordTokenType(304);}
"rules" {return tt.getKeywordTokenType(305);}
"sample" {return tt.getKeywordTokenType(306);}
"save" {return tt.getKeywordTokenType(307);}
"savepoint" {return tt.getKeywordTokenType(308);}
"schema" {return tt.getKeywordTokenType(309);}
"schemacheck" {return tt.getKeywordTokenType(310);}
"scn" {return tt.getKeywordTokenType(311);}
"second" {return tt.getKeywordTokenType(312);}
"seed" {return tt.getKeywordTokenType(313);}
"segment" {return tt.getKeywordTokenType(314);}
"select" {return tt.getKeywordTokenType(315);}
"self" {return tt.getKeywordTokenType(316);}
"separate" {return tt.getKeywordTokenType(317);}
"sequential" {return tt.getKeywordTokenType(318);}
"serializable" {return tt.getKeywordTokenType(319);}
"serially_reusable" {return tt.getKeywordTokenType(320);}
"servererror" {return tt.getKeywordTokenType(321);}
"set" {return tt.getKeywordTokenType(322);}
"sets" {return tt.getKeywordTokenType(323);}
"share" {return tt.getKeywordTokenType(324);}
"show" {return tt.getKeywordTokenType(325);}
"shutdown" {return tt.getKeywordTokenType(326);}
"siblings" {return tt.getKeywordTokenType(327);}
"sig" {return tt.getKeywordTokenType(328);}
"single" {return tt.getKeywordTokenType(329);}
"size" {return tt.getKeywordTokenType(330);}
"some" {return tt.getKeywordTokenType(331);}
"space" {return tt.getKeywordTokenType(332);}
"sql" {return tt.getKeywordTokenType(333);}
"sqlcode" {return tt.getKeywordTokenType(334);}
"sqlerrm" {return tt.getKeywordTokenType(335);}
"standalone" {return tt.getKeywordTokenType(336);}
"start" {return tt.getKeywordTokenType(337);}
"startup" {return tt.getKeywordTokenType(338);}
"statement" {return tt.getKeywordTokenType(339);}
"static" {return tt.getKeywordTokenType(340);}
"statistic" {return tt.getKeywordTokenType(341);}
"statistics" {return tt.getKeywordTokenType(342);}
"struct" {return tt.getKeywordTokenType(343);}
"submultiset" {return tt.getKeywordTokenType(344);}
"subpartition" {return tt.getKeywordTokenType(345);}
"subtype" {return tt.getKeywordTokenType(346);}
"successful" {return tt.getKeywordTokenType(347);}
"sum_squares_between" {return tt.getKeywordTokenType(348);}
"sum_squares_within" {return tt.getKeywordTokenType(349);}
"suspend" {return tt.getKeywordTokenType(350);}
"synonym" {return tt.getKeywordTokenType(351);}
"table" {return tt.getKeywordTokenType(352);}
"tdo" {return tt.getKeywordTokenType(353);}
"then" {return tt.getKeywordTokenType(354);}
"time" {return tt.getKeywordTokenType(355);}
"timezone_abbr" {return tt.getKeywordTokenType(356);}
"timezone_hour" {return tt.getKeywordTokenType(357);}
"timezone_minute" {return tt.getKeywordTokenType(358);}
"timezone_region" {return tt.getKeywordTokenType(359);}
"to" {return tt.getKeywordTokenType(360);}
"trailing" {return tt.getKeywordTokenType(361);}
"transaction" {return tt.getKeywordTokenType(362);}
"trigger" {return tt.getKeywordTokenType(363);}
"truncate" {return tt.getKeywordTokenType(364);}
"trust" {return tt.getKeywordTokenType(365);}
"two_sided_prob" {return tt.getKeywordTokenType(366);}
"two_sided_sig" {return tt.getKeywordTokenType(367);}
"type" {return tt.getKeywordTokenType(368);}
"u_statistic" {return tt.getKeywordTokenType(369);}
"unbounded" {return tt.getKeywordTokenType(370);}
"under" {return tt.getKeywordTokenType(371);}
"under_path" {return tt.getKeywordTokenType(372);}
"union" {return tt.getKeywordTokenType(373);}
"unique" {return tt.getKeywordTokenType(374);}
"unlimited" {return tt.getKeywordTokenType(375);}
"unpivot" {return tt.getKeywordTokenType(376);}
"until" {return tt.getKeywordTokenType(377);}
"update" {return tt.getKeywordTokenType(378);}
"updated" {return tt.getKeywordTokenType(379);}
"updating" {return tt.getKeywordTokenType(380);}
"upsert" {return tt.getKeywordTokenType(381);}
"use" {return tt.getKeywordTokenType(382);}
"user" {return tt.getKeywordTokenType(383);}
"using" {return tt.getKeywordTokenType(384);}
"validate" {return tt.getKeywordTokenType(385);}
"values" {return tt.getKeywordTokenType(386);}
"variable" {return tt.getKeywordTokenType(387);}
"varray" {return tt.getKeywordTokenType(388);}
"varying" {return tt.getKeywordTokenType(389);}
"version" {return tt.getKeywordTokenType(390);}
"versions" {return tt.getKeywordTokenType(391);}
"view" {return tt.getKeywordTokenType(392);}
"wait" {return tt.getKeywordTokenType(393);}
"wellformed" {return tt.getKeywordTokenType(394);}
"when" {return tt.getKeywordTokenType(395);}
"whenever" {return tt.getKeywordTokenType(396);}
"where" {return tt.getKeywordTokenType(397);}
"while" {return tt.getKeywordTokenType(398);}
"with" {return tt.getKeywordTokenType(399);}
"within" {return tt.getKeywordTokenType(400);}
"wnds" {return tt.getKeywordTokenType(401);}
"wnps" {return tt.getKeywordTokenType(402);}
"work" {return tt.getKeywordTokenType(403);}
"write" {return tt.getKeywordTokenType(404);}
"xml" {return tt.getKeywordTokenType(405);}
"xmlnamespaces" {return tt.getKeywordTokenType(406);}
"year" {return tt.getKeywordTokenType(407);}
"yes" {return tt.getKeywordTokenType(408);}
"zone" {return tt.getKeywordTokenType(409);}
"false" {return tt.getKeywordTokenType(410);}
"true" {return tt.getKeywordTokenType(411);}











"abs" {return tt.getFunctionTokenType(0);}
"acos" {return tt.getFunctionTokenType(1);}
"add_months" {return tt.getFunctionTokenType(2);}
"appendchildxml" {return tt.getFunctionTokenType(3);}
"ascii" {return tt.getFunctionTokenType(4);}
"asciistr" {return tt.getFunctionTokenType(5);}
"asin" {return tt.getFunctionTokenType(6);}
"atan" {return tt.getFunctionTokenType(7);}
"atan2" {return tt.getFunctionTokenType(8);}
"avg" {return tt.getFunctionTokenType(9);}
"bfilename" {return tt.getFunctionTokenType(10);}
"bin_to_num" {return tt.getFunctionTokenType(11);}
"bitand" {return tt.getFunctionTokenType(12);}
"cardinality" {return tt.getFunctionTokenType(13);}
"cast" {return tt.getFunctionTokenType(14);}
"ceil" {return tt.getFunctionTokenType(15);}
"chartorowid" {return tt.getFunctionTokenType(16);}
"chr" {return tt.getFunctionTokenType(17);}
"compose" {return tt.getFunctionTokenType(18);}
"concat" {return tt.getFunctionTokenType(19);}
"convert" {return tt.getFunctionTokenType(20);}
"corr" {return tt.getFunctionTokenType(21);}
"corr_k" {return tt.getFunctionTokenType(22);}
"corr_s" {return tt.getFunctionTokenType(23);}
"cos" {return tt.getFunctionTokenType(24);}
"cosh" {return tt.getFunctionTokenType(25);}
"covar_pop" {return tt.getFunctionTokenType(26);}
"covar_samp" {return tt.getFunctionTokenType(27);}
"cume_dist" {return tt.getFunctionTokenType(28);}
"current_date" {return tt.getFunctionTokenType(29);}
"current_timestamp" {return tt.getFunctionTokenType(30);}
"cv" {return tt.getFunctionTokenType(31);}
"dbtimezone" {return tt.getFunctionTokenType(32);}
"dbtmezone" {return tt.getFunctionTokenType(33);}
"decode" {return tt.getFunctionTokenType(34);}
"decompose" {return tt.getFunctionTokenType(35);}
"deletexml" {return tt.getFunctionTokenType(36);}
"depth" {return tt.getFunctionTokenType(37);}
"deref" {return tt.getFunctionTokenType(38);}
"empty_blob" {return tt.getFunctionTokenType(39);}
"empty_clob" {return tt.getFunctionTokenType(40);}
"existsnode" {return tt.getFunctionTokenType(41);}
"exp" {return tt.getFunctionTokenType(42);}
"extract" {return tt.getFunctionTokenType(43);}
"extractvalue" {return tt.getFunctionTokenType(44);}
"first_value" {return tt.getFunctionTokenType(45);}
"floor" {return tt.getFunctionTokenType(46);}
"from_tz" {return tt.getFunctionTokenType(47);}
"greatest" {return tt.getFunctionTokenType(48);}
"group_id" {return tt.getFunctionTokenType(49);}
"grouping" {return tt.getFunctionTokenType(50);}
"grouping_id" {return tt.getFunctionTokenType(51);}
"hextoraw" {return tt.getFunctionTokenType(52);}
"initcap" {return tt.getFunctionTokenType(53);}
"insertchildxml" {return tt.getFunctionTokenType(54);}
"insertchildxmlafter" {return tt.getFunctionTokenType(55);}
"insertchildxmlbefore" {return tt.getFunctionTokenType(56);}
"insertxmlafter" {return tt.getFunctionTokenType(57);}
"insertxmlbefore" {return tt.getFunctionTokenType(58);}
"instr" {return tt.getFunctionTokenType(59);}
"instr2" {return tt.getFunctionTokenType(60);}
"instr4" {return tt.getFunctionTokenType(61);}
"instrb" {return tt.getFunctionTokenType(62);}
"instrc" {return tt.getFunctionTokenType(63);}
"iteration_number" {return tt.getFunctionTokenType(64);}
"lag" {return tt.getFunctionTokenType(65);}
"last_day" {return tt.getFunctionTokenType(66);}
"last_value" {return tt.getFunctionTokenType(67);}
"lead" {return tt.getFunctionTokenType(68);}
"least" {return tt.getFunctionTokenType(69);}
"length" {return tt.getFunctionTokenType(70);}
"length2" {return tt.getFunctionTokenType(71);}
"length4" {return tt.getFunctionTokenType(72);}
"lengthb" {return tt.getFunctionTokenType(73);}
"lengthc" {return tt.getFunctionTokenType(74);}
"listagg" {return tt.getFunctionTokenType(75);}
"ln" {return tt.getFunctionTokenType(76);}
"lnnvl" {return tt.getFunctionTokenType(77);}
"localtimestamp" {return tt.getFunctionTokenType(78);}
"lower" {return tt.getFunctionTokenType(79);}
"lpad" {return tt.getFunctionTokenType(80);}
"ltrim" {return tt.getFunctionTokenType(81);}
"make_ref" {return tt.getFunctionTokenType(82);}
"max" {return tt.getFunctionTokenType(83);}
"median" {return tt.getFunctionTokenType(84);}
"min" {return tt.getFunctionTokenType(85);}
"mod" {return tt.getFunctionTokenType(86);}
"months_between" {return tt.getFunctionTokenType(87);}
"nanvl" {return tt.getFunctionTokenType(88);}
"nchr" {return tt.getFunctionTokenType(89);}
"new_time" {return tt.getFunctionTokenType(90);}
"next_day" {return tt.getFunctionTokenType(91);}
"nls_charset_decl_len" {return tt.getFunctionTokenType(92);}
"nls_charset_id" {return tt.getFunctionTokenType(93);}
"nls_charset_name" {return tt.getFunctionTokenType(94);}
"nls_initcap" {return tt.getFunctionTokenType(95);}
"nls_lower" {return tt.getFunctionTokenType(96);}
"nls_upper" {return tt.getFunctionTokenType(97);}
"nlssort" {return tt.getFunctionTokenType(98);}
"ntile" {return tt.getFunctionTokenType(99);}
"nullif" {return tt.getFunctionTokenType(100);}
"numtodsinterval" {return tt.getFunctionTokenType(101);}
"numtoyminterval" {return tt.getFunctionTokenType(102);}
"nvl" {return tt.getFunctionTokenType(103);}
"nvl2" {return tt.getFunctionTokenType(104);}
"ora_hash" {return tt.getFunctionTokenType(105);}
"percent_rank" {return tt.getFunctionTokenType(106);}
"percentile_cont" {return tt.getFunctionTokenType(107);}
"percentile_disc" {return tt.getFunctionTokenType(108);}
"powermultiset" {return tt.getFunctionTokenType(109);}
"powermultiset_by_cardinality" {return tt.getFunctionTokenType(110);}
"presentnnv" {return tt.getFunctionTokenType(111);}
"presentv" {return tt.getFunctionTokenType(112);}
"previous" {return tt.getFunctionTokenType(113);}
"rank" {return tt.getFunctionTokenType(114);}
"ratio_to_report" {return tt.getFunctionTokenType(115);}
"rawtohex" {return tt.getFunctionTokenType(116);}
"rawtonhex" {return tt.getFunctionTokenType(117);}
"reftohex" {return tt.getFunctionTokenType(118);}
"regexp_instr" {return tt.getFunctionTokenType(119);}
"regexp_replace" {return tt.getFunctionTokenType(120);}
"regexp_substr" {return tt.getFunctionTokenType(121);}
"regr_avgx" {return tt.getFunctionTokenType(122);}
"regr_avgy" {return tt.getFunctionTokenType(123);}
"regr_count" {return tt.getFunctionTokenType(124);}
"regr_intercept" {return tt.getFunctionTokenType(125);}
"regr_r2" {return tt.getFunctionTokenType(126);}
"regr_slope" {return tt.getFunctionTokenType(127);}
"regr_sxx" {return tt.getFunctionTokenType(128);}
"regr_sxy" {return tt.getFunctionTokenType(129);}
"regr_syy" {return tt.getFunctionTokenType(130);}
"round" {return tt.getFunctionTokenType(131);}
"row_number" {return tt.getFunctionTokenType(132);}
"rowidtochar" {return tt.getFunctionTokenType(133);}
"rowidtonchar" {return tt.getFunctionTokenType(134);}
"rpad" {return tt.getFunctionTokenType(135);}
"rtrim" {return tt.getFunctionTokenType(136);}
"scn_to_timestamp" {return tt.getFunctionTokenType(137);}
"sessiontimezone" {return tt.getFunctionTokenType(138);}
"sign" {return tt.getFunctionTokenType(139);}
"sin" {return tt.getFunctionTokenType(140);}
"sinh" {return tt.getFunctionTokenType(141);}
"soundex" {return tt.getFunctionTokenType(142);}
"sqrt" {return tt.getFunctionTokenType(143);}
"stats_binomial_test" {return tt.getFunctionTokenType(144);}
"stats_crosstab" {return tt.getFunctionTokenType(145);}
"stats_f_test" {return tt.getFunctionTokenType(146);}
"stats_ks_test" {return tt.getFunctionTokenType(147);}
"stats_mode" {return tt.getFunctionTokenType(148);}
"stats_mw_test" {return tt.getFunctionTokenType(149);}
"stats_one_way_anova" {return tt.getFunctionTokenType(150);}
"stats_t_test_indep" {return tt.getFunctionTokenType(151);}
"stats_t_test_indepu" {return tt.getFunctionTokenType(152);}
"stats_t_test_one" {return tt.getFunctionTokenType(153);}
"stats_t_test_paired" {return tt.getFunctionTokenType(154);}
"stats_wsr_test" {return tt.getFunctionTokenType(155);}
"stddev" {return tt.getFunctionTokenType(156);}
"stddev_pop" {return tt.getFunctionTokenType(157);}
"stddev_samp" {return tt.getFunctionTokenType(158);}
"substr" {return tt.getFunctionTokenType(159);}
"substr2" {return tt.getFunctionTokenType(160);}
"substr4" {return tt.getFunctionTokenType(161);}
"substrb" {return tt.getFunctionTokenType(162);}
"substrc" {return tt.getFunctionTokenType(163);}
"sum" {return tt.getFunctionTokenType(164);}
"sys_connect_by_path" {return tt.getFunctionTokenType(165);}
"sys_context" {return tt.getFunctionTokenType(166);}
"sys_dburigen" {return tt.getFunctionTokenType(167);}
"sys_extract_utc" {return tt.getFunctionTokenType(168);}
"sys_guid" {return tt.getFunctionTokenType(169);}
"sys_typeid" {return tt.getFunctionTokenType(170);}
"sys_xmlagg" {return tt.getFunctionTokenType(171);}
"sys_xmlgen" {return tt.getFunctionTokenType(172);}
"sysdate" {return tt.getFunctionTokenType(173);}
"systimestamp" {return tt.getFunctionTokenType(174);}
"tan" {return tt.getFunctionTokenType(175);}
"tanh" {return tt.getFunctionTokenType(176);}
"timestamp_to_scn" {return tt.getFunctionTokenType(177);}
"to_binary_double" {return tt.getFunctionTokenType(178);}
"to_binary_float" {return tt.getFunctionTokenType(179);}
"to_char" {return tt.getFunctionTokenType(180);}
"to_clob" {return tt.getFunctionTokenType(181);}
"to_date" {return tt.getFunctionTokenType(182);}
"to_dsinterval" {return tt.getFunctionTokenType(183);}
"to_lob" {return tt.getFunctionTokenType(184);}
"to_multi_byte" {return tt.getFunctionTokenType(185);}
"to_nchar" {return tt.getFunctionTokenType(186);}
"to_nclob" {return tt.getFunctionTokenType(187);}
"to_number" {return tt.getFunctionTokenType(188);}
"to_single_byte" {return tt.getFunctionTokenType(189);}
"to_timestamp" {return tt.getFunctionTokenType(190);}
"to_timestamp_tz" {return tt.getFunctionTokenType(191);}
"to_yminterval" {return tt.getFunctionTokenType(192);}
"translate" {return tt.getFunctionTokenType(193);}
"treat" {return tt.getFunctionTokenType(194);}
"trim" {return tt.getFunctionTokenType(195);}
"trunc" {return tt.getFunctionTokenType(196);}
"tz_offset" {return tt.getFunctionTokenType(197);}
"uid" {return tt.getFunctionTokenType(198);}
"unistr" {return tt.getFunctionTokenType(199);}
"updatexml" {return tt.getFunctionTokenType(200);}
"upper" {return tt.getFunctionTokenType(201);}
"userenv" {return tt.getFunctionTokenType(202);}
"value" {return tt.getFunctionTokenType(203);}
"var_pop" {return tt.getFunctionTokenType(204);}
"var_samp" {return tt.getFunctionTokenType(205);}
"variance" {return tt.getFunctionTokenType(206);}
"vsize" {return tt.getFunctionTokenType(207);}
"width_bucket" {return tt.getFunctionTokenType(208);}
"xmlagg" {return tt.getFunctionTokenType(209);}
"xmlattributes" {return tt.getFunctionTokenType(210);}
"xmlcast" {return tt.getFunctionTokenType(211);}
"xmlcdata" {return tt.getFunctionTokenType(212);}
"xmlcolattval" {return tt.getFunctionTokenType(213);}
"xmlcomment" {return tt.getFunctionTokenType(214);}
"xmlconcat" {return tt.getFunctionTokenType(215);}
"xmldiff" {return tt.getFunctionTokenType(216);}
"xmlelement" {return tt.getFunctionTokenType(217);}
"xmlforest" {return tt.getFunctionTokenType(218);}
"xmlisvalid" {return tt.getFunctionTokenType(219);}
"xmlparse" {return tt.getFunctionTokenType(220);}
"xmlpatch" {return tt.getFunctionTokenType(221);}
"xmlpi" {return tt.getFunctionTokenType(222);}
"xmlquery" {return tt.getFunctionTokenType(223);}
"xmlroot" {return tt.getFunctionTokenType(224);}
"xmlsequence" {return tt.getFunctionTokenType(225);}
"xmlserialize" {return tt.getFunctionTokenType(226);}
"xmltable" {return tt.getFunctionTokenType(227);}
"xmltransform" {return tt.getFunctionTokenType(228);}




"access_into_null" {return tt.getExceptionTokenType(0);}
"case_not_found" {return tt.getExceptionTokenType(1);}
"collection_is_null" {return tt.getExceptionTokenType(2);}
"cursor_already_open" {return tt.getExceptionTokenType(3);}
"dup_val_on_index" {return tt.getExceptionTokenType(4);}
"invalid_cursor" {return tt.getExceptionTokenType(5);}
"invalid_number" {return tt.getExceptionTokenType(6);}
"login_denied" {return tt.getExceptionTokenType(7);}
"no_data_found" {return tt.getExceptionTokenType(8);}
"not_logged_on" {return tt.getExceptionTokenType(9);}
"program_error" {return tt.getExceptionTokenType(10);}
"rowtype_mismatch" {return tt.getExceptionTokenType(11);}
"self_is_null" {return tt.getExceptionTokenType(12);}
"storage_error" {return tt.getExceptionTokenType(13);}
"subscript_beyond_count" {return tt.getExceptionTokenType(14);}
"subscript_outside_limit" {return tt.getExceptionTokenType(15);}
"sys_invalid_rowid" {return tt.getExceptionTokenType(16);}
"timeout_on_resource" {return tt.getExceptionTokenType(17);}
"too_many_rows" {return tt.getExceptionTokenType(18);}
"value_error" {return tt.getExceptionTokenType(19);}
"zero_divide" {return tt.getExceptionTokenType(20);}



{IDENTIFIER}           { return tt.getSharedTokenTypes().getIdentifier(); }
{QUOTED_IDENTIFIER}    { return tt.getSharedTokenTypes().getQuotedIdentifier(); }
.                      { return tt.getSharedTokenTypes().getIdentifier(); }


