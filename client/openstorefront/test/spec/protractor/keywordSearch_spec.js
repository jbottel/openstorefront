describe('keywordSearch_Search for Common Map Widget API', function() {
  it('two search results are returned', function() {
    // Open the main site
    browser.get(theSite);
    
    // Enter the search term (changed to enter after updates to search keys 7/28)
    element(by.id('mainSearchBar')).sendKeys('Common Map Widget API', protractor.Key.ENTER);

    // Should only be two results
    expect(element.all(by.repeater('item in data')).count()).toEqual(2);
  });


});