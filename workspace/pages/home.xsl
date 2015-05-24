<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

		<xsl:import href="../utilities/main.xsl"/>

		<xsl:template name="content">
			<xsl:choose>
				<xsl:when test="$filter = 'yes'">
					<h2>All movies
						<xsl:choose>
							<xsl:when test="$director">
								<text> directed by </text><xsl:apply-templates select="/data/actors/artist[@id = $director]" mode="name"/>
							</xsl:when>
							<xsl:when test="$actor">
								<text> starring </text><xsl:apply-templates select="/data/actors/artist[@id = $actor]" mode="name"/>
							</xsl:when>
							<xsl:when test="$year">
								<text> produced in </text><xsl:value-of select="$year"/>
							</xsl:when>
							<xsl:when test="$genre">
								<text> in the </text><xsl:value-of select="$genre"/><xsl:text> genre</xsl:text>
							</xsl:when>
						</xsl:choose>
					</h2>
					<xsl:apply-templates select="/data/movies/movie[director/@id = $director or actor/@id = $actor or year = $year or genre = $genre]" />
				</xsl:when>
				<xsl:when test="$filter = 'no' and not($keywords = '')">
					<h2>Search Results</h2>
					<xsl:apply-templates select="/data/movies/movie[contains(title, $keywords)]" />
				</xsl:when>
				<xsl:otherwise>
					<h2>All Results</h2>
					<xsl:apply-templates select="/data/movies/movie" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:template>
	</xsl:stylesheet>