<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

		<xsl:import href="../utilities/main.xsl"/>
		<xsl:variable name="movie-title" select="normalize-space(/data/params/movie-title)"/>

		<xsl:template name="content">
			<xsl:apply-templates select="/data/movies/movie[title/@handle = $movie-title]" mode="detailed"/>
		</xsl:template>

		<xsl:template match="movies/movie" mode="detailed">
			<div class="detailed">
				<h2><xsl:value-of select="title"/></h2>
			<p><xsl:value-of select="summary"/></p>
			<p>Directed by: <xsl:apply-templates select="/data/actors/artist[@id = current()/director/@id]" mode="name"/></p>
			<p>Starring: <xsl:for-each select="actor">
					<xsl:apply-templates select="/data/actors/artist[@id = current()/@id]" mode="name"/>
					<xsl:if test="not(position() = last())">, </xsl:if>
				</xsl:for-each></p>
			</div>
		</xsl:template>
	</xsl:stylesheet>