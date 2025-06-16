import React from 'react';
import { Tooltip } from 'react-tooltip';


const VerifiedPurchaseBadge: React.FC = () => (
  <>
    <span
      data-tooltip-id="verified-tooltip"
      data-tooltip-content="Verified Purchase"
      className="ml-2 bg-green-100 text-green-800 text-xs font-medium px-2 py-0.5 rounded flex items-center"
    >
      ✓
    </span>
    <Tooltip id="verified-tooltip" />
  </>
);

export default VerifiedPurchaseBadge;