/**
 * (c) 2003-2016 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.cookbook.pagination;

import com.cookbook.tutorial.service.CookBookEntity;
import com.cookbook.tutorial.service.SessionExpiredException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.api.MuleException;
import org.mule.modules.cookbook.CookbookConnector;
import org.mule.streaming.ProviderAwarePagingDelegate;

import java.util.List;
import java.util.Map;

public class CookbookPagingDelegate extends ProviderAwarePagingDelegate<Map<String, Object>, CookbookConnector> {

    private Integer currentPage = 0;
    private ObjectMapper m = new ObjectMapper();

    private final Integer pageSize;
    private final String query;

    public CookbookPagingDelegate(String query, Integer pageSize) {
        super();
        this.query = query;
        this.pageSize = pageSize;
    }

    public void close() throws MuleException {
        this.currentPage = 0;
    }

    /**
     * Returns the next page of items. If the return value is <code>null</code> or an empty list, then it means no more items are available
     *
     * @param connector
     *            The provider to be used to do the query. You can assume this provider is already properly initialised
     * @return a populated list of elements. Returning <code>null</code> or an empty list, means no more items are available.
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> getPage(CookbookConnector connector) throws Exception {

        try {
            List<CookBookEntity> list = connector.getConfig().getClient().searchWithQuery(query, currentPage++, pageSize);
            return m.convertValue(list, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (SessionExpiredException ex) {
            // Revert the increment since we want to retry to get the same page if the reconnection is configured.
            currentPage--;
            throw ex;
        }
    }

    /**
     * Returns the total amount of items in the non-paged result set.
     * <p/>
     * In some scenarios, it might not be possible/convenient to actually retrieve this value. -1 is returned in such a case.
     *
     * @param provider
     *            The provider to be used to do the query. You can assume this provider is already properly initialised
     */
    @Override
    public int getTotalResults(CookbookConnector provider) throws Exception {
        return -1;
    }

}
