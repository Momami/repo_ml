# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class BookItem(scrapy.Item):
    # define the fields for your item here like:
    title = scrapy.Field()
    author = scrapy.Field()
    price = scrapy.Field()
    year = scrapy.Field()
    count_page = scrapy.Field()
    publisher = scrapy.Field()
    annotation = scrapy.Field()
