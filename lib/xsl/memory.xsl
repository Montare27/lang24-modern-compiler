<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="memory">
  <html>
    <style>
      table, tr, td {
      text-align: center;
      vertical-align: top;
      }
    </style>
    <body>
      <table>
	<xsl:apply-templates select="node"/>
      </table>
    </body>
  </html>
</xsl:template>

<xsl:template match="node">
  <td>
    <table width="100%">
      <tr bgcolor="DDDDDD">
	<td colspan="1000">
	  <nobr>
	    <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	    (<xsl:value-of select="@id"/>)
	    <font style="font-family:arial black">
	      <xsl:value-of select="@label"/>
	    </font>
	    <xsl:if test="@name!=''">
	      <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	      <font style="font-family:helvetica">
		<xsl:value-of select="@name"/>
	      </font>
	    </xsl:if>
	    <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	  </nobr>
	  <br/>
	  <xsl:if test="location">
	    <nobr>
	      <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	      <xsl:apply-templates select="location"/>
	      <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	    </nobr>
	  </xsl:if>
          <table width="100%">
            <xsl:apply-templates select="definedat"/>
	    <xsl:if test="istype">
	      <tr bgcolor="FFAF11">
		<td>
		  <font style="font-family:arial black" size="1">
		    ISTYPE:
		  </font>
		</td>
	      </tr>
	      <xsl:apply-templates select="istype"/>
	    </xsl:if>
	    <xsl:if test="lvalue">
	       <tr bgcolor="FFAF11">
		<td>
		  <font style="font-family:arial black" size="1">
		    LVALUE
		  </font>
		</td>
	      </tr>
	    </xsl:if>
	    <xsl:if test="oftype">
	      <tr bgcolor="FFAF11">
		<td>
		  <font style="font-family:arial black" size="1">
		    OFTYPE:
		  </font>
		</td>
	      </tr>
              <xsl:apply-templates select="oftype"/>
	    </xsl:if>
            <xsl:apply-templates select="addr"/>
            <xsl:apply-templates select="frame"/>
            <xsl:apply-templates select="access"/>
	  </table>
	</td>
      </tr>
      <tr>
	<xsl:apply-templates select="node"/>
      </tr>
    </table>
  </td>
</xsl:template>

<xsl:template match="definedat">
  <tr bgcolor="FFCF00">
    <td>
      <nobr>
	<xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	(<xsl:value-of select="@id"/>)
	[<xsl:value-of select="@location"/>]
	<xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
      </nobr>
    </td>
  </tr>	
</xsl:template>

<xsl:template match="istype">
  <tr>
     <xsl:apply-templates select="type"/>
  </tr>
</xsl:template>
  
<xsl:template match="type">
  <td>
    <table width="100%">
      <tr bgcolor="FFAF11">
	<td colspan="1000">
	  <nobr>
	    <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	    (<xsl:value-of select="@id"/>)
	    <xsl:value-of select="@label"/>
	    <xsl:if test="@name!=''">
	      <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	      <font style="font-family:helvetica">
		<xsl:value-of select="@name"/>
	      </font>
	    </xsl:if>
	    <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	  </nobr>
	</td>
      </tr>
      <tr>
	<xsl:apply-templates select="type"/>
      </tr>
    </table>
  </td>
</xsl:template>

<xsl:template match="addr">
  <tr bgcolor="FFCF00">
    <td>
      <nobr>
	<xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
	addr
	<xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
      </nobr>
    </td>
  </tr>	
</xsl:template>

<xsl:template match="frame">
  <tr>
    <td>
      <table width="100%" bgcolor="EECF00">
	<tr>
	  <td>
	    <font style="font-family:arial black" size="1">
	      FRAME:
	    </font>
	  </td>
	</tr>
	<tr>
	  <td>
	    <nobr>
	      label=<font style="font-family:courier new"><xsl:value-of select="@label"/></font>
	      depth=<xsl:value-of select="@depth"/> 
	      size=<xsl:value-of select="@size"/> 
	      locs=<xsl:value-of select="@locssize"/>
	      args=<xsl:value-of select="@argssize"/>
	      FP=<xsl:value-of select="@FP"/>
	      RV=<xsl:value-of select="@RV"/>
	    </nobr>
	  </td>
	</tr>
      </table>
    </td>
  </tr>
</xsl:template>

<xsl:template match="access">
  <tr>
    <td>
      <table width="100%" bgcolor="EECF00">
	<tr>
	  <td>
	    <font style="font-family:arial black" size="1">
	      ACCESS:
	    </font>
	  </td>
	</tr>
	<tr>
	  <td>
	    <nobr>
	      size=<xsl:value-of select="@size"/> 
	      <xsl:if test="@label!=''">
		label=<font style="font-family:courier new"><xsl:value-of select="@label"/></font>
	      </xsl:if>
	      <xsl:if test="@init!=''">
		init=<font style="font-family:courier new"><xsl:value-of select="@init"/></font>
	      </xsl:if>
	      <xsl:if test="@offset!=''">
		offset=<xsl:value-of select="@offset"/>
	      </xsl:if>
	      <xsl:if test="@depth!=''">
		depth=<xsl:value-of select="@depth"/>
	      </xsl:if>
	    </nobr>
	  </td>
	</tr>
      </table>
    </td>
  </tr>
</xsl:template>

<xsl:template match="location">
  <nobr>
    <font style="font-family:helvetica">
      <xsl:value-of select="@loc"/>
    </font>
  </nobr>
</xsl:template>

</xsl:stylesheet>
