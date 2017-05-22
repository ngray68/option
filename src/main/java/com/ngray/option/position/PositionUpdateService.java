package com.ngray.option.position;

import com.ngray.option.ig.position.IGPositionUpdate;
import com.ngray.option.service.Service;
import com.ngray.option.service.ServiceDataSource;

/**
 * PositionUpdateService
 * Provides position updates for a specific account id
 * @author nigelgray
 *
 */
public class PositionUpdateService extends Service<String, IGPositionUpdate> {

	public PositionUpdateService(String name, ServiceDataSource<String, IGPositionUpdate> dataSource) {
		super(name, dataSource);
	}
}
