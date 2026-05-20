package com.auction.client.screenhandler;

import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.response.AuctionResponseDTO;

/**
 * Interface định nghĩa khả năng điều hướng đến màn hình chi tiết sản phẩm.
 * Các Controller muốn điều hướng đến chi tiết sản phẩm sẽ implement interface này.
 */
public interface ProductDetailNavigator {
  void gotoProductDetail(AuctionDTO selectedAuction);
}
