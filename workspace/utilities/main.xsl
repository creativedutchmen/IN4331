<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml"
		doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
		omit-xml-declaration="yes"
		encoding="UTF-8"
		indent="yes" />

		<xsl:variable name="keywords" select="normalize-space(/data/params/url-keywords)"/>
		<xsl:variable name="by" select="normalize-space(/data/params/url-by)"/>
		<xsl:variable name="director" select="normalize-space(/data/params/url-director)"/>
		<xsl:variable name="year" select="normalize-space(/data/params/url-year)"/>
		<xsl:variable name="actor" select="normalize-space(/data/params/url-actor)"/>
		<xsl:variable name="genre" select="normalize-space(/data/params/url-genre)"/>
		<xsl:variable name="filter">
			<xsl:choose>
				<xsl:when test="not($director='' and $year='' and $actor='' and $genre='')">yes</xsl:when>
				<xsl:otherwise>no</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:template match="/">
			<html>
				<head>
					<link href="{$root}/workspace/assets/css/style.css" rel="stylesheet" type="text/css" media="all" />
					<meta name="viewport" content="width=device-width, initial-scale=1" />
				</head>
				<body>
					<form method="get" action="">
						<input type="text" name="keywords" value="{$keywords}" />
						<button type="submit"><span>Search</span></button>
					</form>
					<div id="filters">
						<h2>Filters</h2>
						<ul class="filters">
							<li class="everything">
								<a href="{$root}">All Movies</a>
							</li>
							<li class="genre">
								<span>Genre</span>
								<ul>
									<xsl:apply-templates select="/data/movies/movie/genre[not(.=preceding::genre)]" mode="filter">
										<xsl:sort select="." />
									</xsl:apply-templates>
								</ul>
							</li>
							<li class="director">
								<span>Director</span>
								<ul>
									<xsl:apply-templates select="/data/actors/artist[@id = /data/movies/movie/director/@id]" mode="director-filter">
										<xsl:sort select="last_name" />
									</xsl:apply-templates>
								</ul>
							</li>
							<li class="actor">
								<span>Actor</span>
								<ul>
									<xsl:apply-templates select="/data/actors/artist[@id = /data/movies/movie/actor/@id]" mode="actor-filter">
										<xsl:sort select="last_name" />
									</xsl:apply-templates>
								</ul>
							</li>
							<li class="genre">
								<span>Year</span>
								<ul>
									<xsl:apply-templates select="/data/movies/movie/year[not(.=preceding::year)]" mode="filter">
										<xsl:sort select="." />
									</xsl:apply-templates>
								</ul>
							</li>
						</ul>
					</div>
					<div id="results">
						<xsl:call-template name="content"/>						
					</div>
				</body>
			</html>
		</xsl:template>

		<xsl:template match="genre" mode="filter">
			<li><a href="{$root}/?genre={.}"><xsl:value-of select="."/></a></li>
		</xsl:template>

		<xsl:template match="year" mode="filter">
			<li><a href="{$root}/?year={.}"><xsl:value-of select="."/></a></li>
		</xsl:template>

		<xsl:template match="artist" mode="director-filter">
			<li><a href="{$root}/?director={@id}">
				<xsl:apply-templates select="." mode="name"/>
			</a></li>
		</xsl:template>

		<xsl:template match="artist" mode="actor-filter">
			<li><a href="{$root}/?actor={@id}">
				<xsl:apply-templates select="." mode="name"/>
			</a></li>
		</xsl:template>

		<xsl:template match="artist" mode="name">
			<xsl:value-of select="first_name"/>&#160;<xsl:value-of select="last_name"/>
		</xsl:template>

		<xsl:template match="movies/movie">
			<div class="result">
				<h3 class="title"><a href="{$root}/movie/{title/@handle}"><xsl:value-of select="title"/> (<xsl:value-of select="year"/>)</a></h3>
				<p class="summary"><xsl:value-of select="substring(summary, 0, 250)"/><xsl:if test="string-length(summary) &gt; 250">&#8230;</xsl:if></p>
				<p class="director"><span>Director: </span><xsl:apply-templates select="/data/actors/artist[@id = current()/director/@id]" mode="name"/></p>
				<p class="actors"><span>Starring: </span><xsl:for-each select="actor">
					<xsl:apply-templates select="/data/actors/artist[@id = current()/@id]" mode="name"/>
					<xsl:if test="not(position() = last())">, </xsl:if>
				</xsl:for-each></p>
			</div>
		</xsl:template>
	</xsl:stylesheet>