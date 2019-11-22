import scrapy
from urllib.parse import urljoin


class PycoderItem(scrapy.Item):
    title = scrapy.Field()
    body = scrapy.Field()
    date = scrapy.Field()

class PycoderSpider(scrapy.Spider):
    name = "pycoder"
    start_urls = [
        'http://pycoder.ru/?page=1',
    ]
    visited_urls = []

    def parse(self, response):
        if response.url not in self.visited_urls:
            self.visited_urls.append(response.url)
            for post_link in response.xpath(
                    '//div[@class="post mb-2"]/h2/a/@href').extract():
                url = urljoin(response.url, post_link)
                yield response.follow(url, callback=self.parse_post)

            next_pages = response.xpath(
                    '//li[contains(@class, "page-item") and'
                    ' not(contains(@class, "active"))]/a/@href').extract()
            next_page = next_pages[-1]

            next_page_url = urljoin(response.url+'/', next_page)
            yield response.follow(next_page_url, callback=self.parse)

    def parse_post(self, response):
        item = PycoderItem()
        title = response.xpath('//div[contains(@class, "col-sm-9")]/h2/text()').extract()
        item['title'] = title
        body = response.xpath('//div[@class="block-paragraph"]//p/text()').extract()
        item['body'] = body
        date = response.xpath('//div[contains(@class, "col-sm-9")]/p/text()').extract()
        item['date'] = date
        yield item