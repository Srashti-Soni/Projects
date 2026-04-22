from flask import Flask, request, jsonify
import numpy as np
import datetime
import warnings
warnings.filterwarnings('ignore')

app = Flask(__name__)

def calculate_fraud_score(amount, payment_type, location, hour):
    score = 0.0
    reasons = []

    # ── AMOUNT SCORING ──
    if payment_type.upper() == 'UPI':
        if amount <= 10000:
            score += 0.0
        elif amount <= 50000:
            score += 0.15
        elif amount <= 100000:
            score += 0.30
        else:
            score += 0.50
            reasons.append("Very high UPI amount")

    elif payment_type.upper() == 'CARD':
        if amount <= 50000:
            score += 0.0
        elif amount <= 100000:
            score += 0.20
        else:
            score += 0.40
            reasons.append("Very high card transaction")

    elif payment_type.upper() == 'WALLET':
        # RBI wallet limit 10000 hai unverified users ke liye
        if amount <= 5000:
            score += 0.0
        elif amount <= 10000:
            score += 0.10
        elif amount <= 20000:
            score += 0.35
            reasons.append("Wallet amount exceeds RBI limit")
        else:
            score += 0.55
            reasons.append("Wallet amount far exceeds RBI limit")

    # ── LOCATION SCORING ──
    if location.upper() == 'INTERNATIONAL':
        if payment_type.upper() == 'WALLET':
            score += 0.30
            reasons.append("International wallet transaction")
        elif payment_type.upper() == 'UPI':
            score += 0.25
            reasons.append("International UPI transaction")
        else:
            score += 0.15
            reasons.append("International transaction")

    # ── TIME SCORING ──
    if hour >= 1 and hour <= 4:
        score += 0.15
        reasons.append("Late night transaction")
    elif hour >= 23 or hour == 0:
        score += 0.10

    # ── CAP AT 1.0 ──
    final_score = round(min(score, 1.0), 4)
    return final_score, reasons


@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        'status': 'running',
        'model': 'SmartFraudScorer v2',
        'description': 'RBI-compliant fraud detection engine'
    })


@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()

        amount = float(data.get('amount', 0))
        location = data.get('location', 'DOMESTIC')
        payment_type = data.get('paymentType', 'UPI')
        current_hour = datetime.datetime.now().hour

        fraud_score, reasons = calculate_fraud_score(
            amount, payment_type, location, current_hour
        )

        if fraud_score >= 0.75:
            decision = 'HELD'
            reason = ' + '.join(reasons) if reasons else \
                'High risk transaction pattern'
        elif fraud_score >= 0.45:
            decision = 'FLAGGED'
            reason = ' + '.join(reasons) if reasons else \
                'Suspicious transaction pattern'
        else:
            decision = 'APPROVED'
            reason = None

        print(f"₹{amount:,.0f} | {payment_type} | "
              f"{location} | Score: {fraud_score} | {decision}")

        return jsonify({
            'fraudScore': fraud_score,
            'decision': decision,
            'reason': reason,
            'model': 'SmartFraudScorer v2',
            'breakdown': reasons
        })

    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({'error': str(e)}), 500


if __name__ == '__main__':
    print("Starting Smart Fraud ML Service on port 5001...")
    app.run(host='0.0.0.0', port=5001, debug=True)